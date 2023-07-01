package com.vljx.hawkspeed.ui.screens.authenticated.world.race

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.exc.race.NoLocationException
import com.vljx.hawkspeed.domain.exc.race.StartRaceFailedException
import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.requestmodels.race.RequestCancelRace
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.requestmodels.race.RequestStartRace
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.usecase.race.GetRaceUseCase
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendCancelRaceRequestUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendStartRaceRequestUseCase
import com.vljx.hawkspeed.domain.usecase.track.GetTrackWithPathUseCase
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.NewRaceIntentState.CancelRace.Companion.CANCELLED_BY_USER
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.NewRaceIntentState.CancelRace.Companion.CANCEL_FALSE_START
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.NewRaceIntentState.CancelRace.Companion.CANCEL_RACE_REASON_NO_LOCATION
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.NewRaceIntentState.CancelRace.Companion.CANCEL_RACE_SERVER_REFUSED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WorldMapRaceViewModel @Inject constructor(
    private val getRaceUseCase: GetRaceUseCase,
    private val getTrackWithPathUseCase: GetTrackWithPathUseCase,

    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val sendStartRaceRequestUseCase: SendStartRaceRequestUseCase,
    private val sendCancelRaceRequestUseCase: SendCancelRaceRequestUseCase
): ViewModel() {
    /**
     * A mutable shared flow, configured to replay a single value; this is the UID of the Track we will be racing.
     */
    private val mutableTrackUid: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * A mutable state flow for the current ongoing race's UID. This is to be set by the function that successfully requests a new race be started.
     */
    private val mutableOngoingRaceUid: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * A mutable shared flow for manual updates to the world map race UI state. Configured to refrain from replaying values.
     */
    private val mutableWorldMapRaceUiState: MutableSharedFlow<WorldMapRaceUiState> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    /**
     * A mutable state flow for an intent to start a new race. Setting this to NewCountdown will immediately begin a new race,
     * so ensure there are no ongoing races prior to allowing this.
     */
    private val mutableNewRaceIntentState: MutableStateFlow<NewRaceIntentState> = MutableStateFlow(NewRaceIntentState.Idle)

    /**
     * Map the ongoing race UID to a Race instance. If null is returned, there is no race at all. Otherwise, if the race is finished, cancelled or disqualified,
     * this is a not racing state. Otherwise, this is a racing state.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val raceStateFromCache: Flow<RaceState> =
        mutableOngoingRaceUid.flatMapLatest { raceUid ->
            if(raceUid.isNullOrBlank()) {
                // Return a flow that simply emits NoRace.
                return@flatMapLatest flow<RaceState> {
                    emit(RaceState.NoRace)
                }
            }
            // Otherwise, get race from cache.
            getRaceUseCase(
                RequestGetRace(raceUid)
            ).map { race ->
                // If race is null, return no race.
                if(race == null) {
                    return@map RaceState.NoRace
                }
                // Otherwise, if race is finished, cancelled or disqualified, emit not racing.
                if(race.isFinished || race.isCancelled || race.isDisqualified) {
                    return@map RaceState.NotRacing(race)
                }
                // Otherwise, we are racing.
                return@map RaceState.Racing(race)
            }
        }

    /**
     * Flat map the latest intent for a new race to a race state. If the state is a new countdown, the countdown will be run, but can be cancelled by an overlapping
     * emission to the mutable new race intent state. If disqualification, failed start will be emitted. Otherwise, no race state will be emitted - communicating the
     * need for a race from somewhere other than new race.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val newRaceState: Flow<RaceState> =
        mutableNewRaceIntentState.flatMapLatest { newRaceIntent ->
            flow {
                when(newRaceIntent) {
                    is NewRaceIntentState.NewCountdown -> {
                        // Loop four times, each time emitting a new countdown state for the counter given.
                        for(i in 0 until 4) {
                            Timber.d("Emitting new countdown second: $i")
                            emit(RaceState.StartingRace(i, newRaceIntent.countdownLocation))
                            delay(1000)
                        }
                    }
                    is NewRaceIntentState.CancelRace -> {
                        // TODO: proper reason string/value here.
                        emit(RaceState.FailedStart(newRaceIntent.reason, newRaceIntent.resourceError))
                    }
                    else -> emit(RaceState.NoRace)
                }
            }
        }

    /**
     * Get location updates.
     */
    val currentLocation: StateFlow<PlayerPosition?> =
        getCurrentLocationUseCase(Unit)

    /**
     * Flat map the selected track's UID to a resource for the track in question, and its path. We'll also share this flow, since we will refer to it
     * in multiple dependant flows.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val trackWithPathResource: SharedFlow<Resource<TrackWithPath>> =
        mutableTrackUid.flatMapLatest { trackUid ->
            getTrackWithPathUseCase(
                RequestGetTrackWithPath(trackUid)
            )
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    /**
     * Map the current location and the resource for a track and its path to a boolean indicating whether or not the Player can start the race. This
     * refers specifically to the Player's orientation and position.
     */
    private val startLineState: StateFlow<StartLineState> =
        combine(
            currentLocation,
            trackWithPathResource
        ) { location, resource ->
            // If location is null, or if resource is not success, or if data inside the success is null, we'll return standby.
            if(location == null || resource.status != Resource.Status.SUCCESS || resource.data == null) {
                return@combine StartLineState.Inconclusive
            }
            // Now, get the track.
            val track: Track = resource.data!!.track
            // Get the distance to the start point here.
            val distanceToStart: Float = track.distanceToStartPointFor(location.latitude, location.longitude)
            // Determine whether orientation is correct.
            val isOrientationCorrect: Boolean = track.isOrientationCorrectFor(location.rotation)
            Timber.d("Checking location: distance: ${distanceToStart}m, orientation: $isOrientationCorrect")
            return@combine when {
                /**
                 * When distance to start is within 10m and orientation is correct, we are in perfect position.
                 */
                distanceToStart <= 10f && isOrientationCorrect ->
                    StartLineState.Perfect(location)

                /**
                 * When distance to start is greater than 10m but less or equal to 30m, we are in a standby position.
                 */
                distanceToStart > 10f && distanceToStart <= 30 ->
                    StartLineState.Standby(location)

                /**
                 * Otherwise, we are no longer able to consider a race for this track.
                 */
                else -> StartLineState.MovedAway(location)
            }
        }.distinctUntilChanged { oldState, newState ->
            // Attach a distinct until changed that will function on the actual types of each state instead of the contents thereof,
            // this will stop race countdowns being interrupted for duplicate 'Perfect' start line state.
            oldState::class.java == newState::class.java
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), StartLineState.Inconclusive)

    /**
     * A merged flow of both the race state as taken from cache and a race state as taken from mutable intent to begin a new race.
     *
     * TODO: trialling this as a state flow.
     */
    private val raceState: StateFlow<RaceState> =
        merge(
            raceStateFromCache,
            newRaceState
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RaceState.NoRace)

    /**
     * Now, combine the race state, current location and the track path with resource to map this all to the most appropriate UI state.
     */
    private val innerWorldMapRaceUiState: SharedFlow<WorldMapRaceUiState> =
        combineTransform(
            raceState,
            trackWithPathResource,
            startLineState
        ) { race, resource, startLine ->
            when {
                /**
                 * Consider new race intent immediately. If it is in start countdown, but the User no longer qualifies for a new race, emit an early disqualification to
                 * the mutable new race intent, then return the loading state from here.
                 */
                race is RaceState.StartingRace && (startLine !is StartLineState.Perfect || resource.status != Resource.Status.SUCCESS || resource.data == null) -> {
                    Timber.d("Cancelling countdown!")
                    emit(WorldMapRaceUiState.Loading)
                    // Disqualify the User by emitting an early disqualification state to our new race intent, which should cause the EarlyDisqualification branch to be triggered.
                    mutableNewRaceIntentState.value = NewRaceIntentState.CancelRace(
                        CANCEL_FALSE_START
                    )
                }

                /**
                 * If start line state is inconclusive, or track resource is loading, emit loading state.
                 */
                startLine is StartLineState.Inconclusive || resource.status == Resource.Status.LOADING ->
                    emit(WorldMapRaceUiState.Loading)

                /**
                 * If track/path resource has failed, and has an ERROR status, we'll emit the load failed state.
                 */
                resource.status == Resource.Status.ERROR ->
                    emit(WorldMapRaceUiState.LoadFailed(resource.resourceError!!))

                /**
                 * If resource has a null path, emit an error that communicates this track does not have a path.
                 * TODO: complete this error handle.
                 */
                resource.data!!.path == null ->
                    throw NotImplementedError("Failed to get worldMapRaceUiState - there is no track path for the desired track. This is not supported.")

                /**
                 * If new race intent is early disqualification, emit a disqualification UI state.
                 */
                race is RaceState.FailedStart -> {
                    // Race failed to start, determine why based on the reason code.
                    when(race.reasonString) {
                        /**
                         * If cancelled by User, be sure to transform the UI state outcome to the Cancelled state.
                         */
                        CANCELLED_BY_USER ->
                            emit(
                                WorldMapRaceUiState.Cancelled(
                                    null,
                                    resource.data!!.track,
                                    resource.data!!.path!!
                                )
                            )

                        /**
                         * If server refused, this means there could be a resource error to pass along, too.
                         * TODO: implement this.
                         */
                        CANCEL_RACE_SERVER_REFUSED ->
                            throw NotImplementedError("Failed to start a race. Server has refused the new race, but this is not yet handled in worldMapRaceViewModel.")

                        /**
                         * If we lost location, just as we finished countdown and moved into starting the race, fail for that reason.
                         * TODO: implement this.
                         */
                        CANCEL_RACE_REASON_NO_LOCATION ->
                            throw NotImplementedError("Failed to start a race. We lost location just as we received approval to start racing. Also, this is not handled in worldMapRaceViewModel.")

                        /**
                         * By default, it is disqualified.
                         */
                        else -> emit(WorldMapRaceUiState.CountdownDisqualified(startLine))
                    }
                }

                /**
                 * If we are starting a race, simply emit all other second counts before 4. At second three, we want to create the race on the server, receiving back
                 * a response. If the response is successful, we'll then emit second 4 from here and immediately try emit the selected race. At this point, the User can actually move without being disqualified.
                 */
                race is RaceState.StartingRace -> {
                    try {
                        if(race.currentSecond == 3) {
                            // We require a current location to continue. Disqualify the countdown if this does not exist.
                            val location: PlayerPosition = currentLocation.value
                                ?: throw NoLocationException()
                            // Current second is 3, just before GO. This is where we'll send for a new race and emit the result to the  mutable state flow for ongoing race UID.
                            Timber.d("Requesting to begin the new race NOW!")
                            // TODO: request start a new race with location above as startLocation and countdownLocation as given by the starting race object.
                            // TODO: do this async so as to not block our countdown logic.
                            // Create a new request to start a race.
                            val requestStartRace = RequestStartRace(
                                resource.data!!.track.trackUid,
                                RequestPlayerUpdate(location),
                                RequestPlayerUpdate(race.countdownLocation)
                            )
                            // Now execute the actual request in a separate coroutine.
                            // TODO: execute in separate coroutine, for now, we'll just run it here.
                            val startRaceResult = sendStartRaceRequestUseCase(
                                requestStartRace
                            )
                            // TODO: handle race result letting us know server failed to start a race for us.
                            if(!startRaceResult.isStarted) {
                                throw NotImplementedError("WorldMapRaceViewModel failed to start race with server, and this is not handled.")
                            }
                            // Now, immediately emit second 3.
                            emit(
                                WorldMapRaceUiState.CountingDown(
                                    3,
                                    race.countdownLocation,
                                    resource.data!!.track,
                                    resource.data!!.path!!
                                )
                            )
                            // Delay for 1000 ms.
                            delay(1000)
                            // Now, emit second 4 and the race UID at the same time.
                            emit(
                                WorldMapRaceUiState.CountingDown(
                                    4,
                                    race.countdownLocation,
                                    resource.data!!.track,
                                    resource.data!!.path!!
                                )
                            )
                            // Now try emit the ongoing race's UID.
                            mutableOngoingRaceUid.tryEmit(startRaceResult.race!!.raceUid)
                        } else {
                            // Always emit the current second as a countdown update.
                            emit(
                                WorldMapRaceUiState.CountingDown(
                                    race.currentSecond,
                                    race.countdownLocation,
                                    resource.data!!.track,
                                    resource.data!!.path!!
                                )
                            )
                        }
                    } catch(nle: NoLocationException) {
                        Timber.d("Cancelling countdown because no current location when race being created!")
                        emit(WorldMapRaceUiState.Loading)
                        // Emit a cancel race state to our mutable new race intent with reason no location.
                        mutableNewRaceIntentState.value = NewRaceIntentState.CancelRace(
                            NewRaceIntentState.CancelRace.CANCEL_RACE_REASON_NO_LOCATION
                        )
                    } catch(srfe: StartRaceFailedException) {
                        Timber.d("Cancelling countdown server informed us race could not be started!")
                        emit(WorldMapRaceUiState.Loading)
                        // Emit a cancel race state to our mutable new race intent with reason server refused and pass the resource error along.
                        // TODO: modify both server and client to support the returning of an actual resource error, for now, we'll raise not implemented.
                        mutableNewRaceIntentState.value = NewRaceIntentState.CancelRace(
                            CANCEL_RACE_SERVER_REFUSED
                        )
                    } catch(e: Exception) {
                        Timber.e(e)
                    }
                }

                /**
                 * There is no race ongoing or starting. Simply present the start line state.
                 */
                race is RaceState.NoRace ->
                    emit(
                        WorldMapRaceUiState.OnStartLine(
                            startLine,
                            resource.data!!.track,
                            resource.data!!.path!!
                        )
                    )

                /**
                 * If we have an ongoing race, emit the racing state.
                 */
                race is RaceState.Racing ->
                    emit(
                        WorldMapRaceUiState.Racing(
                            race.race,
                            resource.data!!.track,
                            resource.data!!.path!!
                        )
                    )

                /**
                 * If we have a not racing state, this means we have completed a race. So differentiate on the race itself to determine which state specifically.
                 */
                race is RaceState.NotRacing ->
                    emit(
                        when {
                            race.race.isFinished -> WorldMapRaceUiState.Finished(
                                race.race,
                                resource.data!!.track,
                                resource.data!!.path!!
                            )
                            race.race.isDisqualified -> WorldMapRaceUiState.Disqualified(
                                race.race,
                                resource.data!!.track,
                                resource.data!!.path!!
                            )
                            race.race.isCancelled -> WorldMapRaceUiState.Cancelled(
                                race.race,
                                resource.data!!.track,
                                resource.data!!.path!!
                            )

                            else -> throw NotImplementedError("Failed to get worldMapRaceUiState - ongoingracestate is 'NotRacing' yet the race is not any of; finished, cancelled or disqualified.")
                        }
                    )
                else -> throw NotImplementedError("Failed to get worldMapRaceUiState - unknown state reached in outer 'when' statement.")
            }
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    /**
     * Publicise a merging of the manual and automatic UI flows.
     */
    val worldMapRaceUiState: StateFlow<WorldMapRaceUiState> =
        merge(
            innerWorldMapRaceUiState,
            mutableWorldMapRaceUiState
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WorldMapRaceUiState.Loading)

    /**
     * Set the track UID for the track we want to race.
     */
    fun setTrackUid(trackUid: String) {
        mutableTrackUid.tryEmit(trackUid)
    }

    /**
     * Start a new race. This function will invoke a countdown, that will be executed right in this function. Immediately, a counting down state will be emitted, 3
     * delays each lasting one second will execute. Between each, a new counting down state will be emitted.
     */
    fun startRace(
        track: Track,
        trackPath: TrackPath,
        countdownPosition: PlayerPosition
    ) {
        viewModelScope.launch {
            // Emit a new countdown state to the mutable start countdown flow.
            mutableNewRaceIntentState.emit(
                NewRaceIntentState.NewCountdown(
                    countdownPosition
                )
            )
        }
    }

    /**
     * Cancel the ongoing race. If the countdown has only started, this will simply abort the countdown. Otherwise, this will cancel the race with the server.
     */
    fun cancelOngoingRace() {
        viewModelScope.launch {
            // Emit a loading state to close down the UI.
            mutableWorldMapRaceUiState.emit(WorldMapRaceUiState.Loading)
            // Get the current value of race state, which will determine how to cancel the race.
            when(val race = raceState.value) {
                /**
                 * If we are currently racing, we will need to cancel this race via the server, then await its response.
                 */
                is RaceState.Racing ->
                    sendCancelRaceRequestUseCase(
                        RequestCancelRace()
                    )

                /**
                 * If we are currently starting a race, simply emit a cancel state to the mutable race intent.
                 */
                is RaceState.StartingRace ->
                    mutableNewRaceIntentState.value = NewRaceIntentState.CancelRace(
                        CANCELLED_BY_USER
                    )

                /**
                 * Otherwise, there's no need to do anything.
                 */
                else -> {

                }
            }
        }
    }

    /**
     * If a race countdown has been disqualified, call this function to reset the race intent to idle, which will release UI to again display OnStartLine
     * when applicable.
     */
    fun resetCountdownDisqualified() {
        mutableNewRaceIntentState.tryEmit(
            NewRaceIntentState.Idle
        )
    }
}
