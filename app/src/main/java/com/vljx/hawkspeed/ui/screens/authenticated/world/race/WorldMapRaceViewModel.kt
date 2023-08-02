package com.vljx.hawkspeed.ui.screens.authenticated.world.race

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.exc.race.NoLocationException
import com.vljx.hawkspeed.domain.exc.race.NoTrackPathException
import com.vljx.hawkspeed.domain.exc.race.NoTrackPathException.Companion.NO_TRACK_PATH
import com.vljx.hawkspeed.domain.exc.race.StartRaceFailedException
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.models.vehicle.OurVehicles
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.models.world.CurrentPlayer
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation
import com.vljx.hawkspeed.domain.requestmodels.race.RequestCancelRace
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.requestmodels.race.RequestStartRace
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.usecase.account.GetCachedAccountUseCase
import com.vljx.hawkspeed.domain.usecase.account.GetSettingsUseCase
import com.vljx.hawkspeed.domain.usecase.race.GetCachedLeaderboardEntryForRaceUseCase
import com.vljx.hawkspeed.domain.usecase.race.GetRaceUseCase
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationAndOrientationUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendCancelRaceRequestUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendStartRaceRequestUseCase
import com.vljx.hawkspeed.domain.usecase.track.GetTrackWithPathUseCase
import com.vljx.hawkspeed.domain.usecase.vehicle.GetOurVehiclesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WorldMapRaceViewModel @Inject constructor(
    getCachedAccountUseCase: GetCachedAccountUseCase,
    getSettingsUseCase: GetSettingsUseCase,
    getOurVehiclesUseCase: GetOurVehiclesUseCase,
    getCurrentLocationAndOrientationUseCase: GetCurrentLocationAndOrientationUseCase,

    private val getRaceUseCase: GetRaceUseCase,
    private val getCachedLeaderboardEntryForRaceUseCase: GetCachedLeaderboardEntryForRaceUseCase,
    private val getTrackWithPathUseCase: GetTrackWithPathUseCase,
    private val sendStartRaceRequestUseCase: SendStartRaceRequestUseCase,
    private val sendCancelRaceRequestUseCase: SendCancelRaceRequestUseCase,

    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
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
     * Get the current User's full list of Vehicles.
     */
    private val ourVehicles: Flow<Resource<OurVehicles>> =
        getOurVehiclesUseCase(Unit)

    /**
     * Get the current account cached in storage. We'll configure this as a state flow.
     */
    private val currentCachedAccount: StateFlow<Account?> =
        getCachedAccountUseCase(Unit)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * A flow that will collect all game settings.
     */
    private val gameSettings: StateFlow<GameSettings?> =
        getSettingsUseCase(Unit)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Get changes for the device's current position, and orientation angles. Configure this as a shared flow.
     */
    private val innerCurrentLocationWithOrientation: SharedFlow<PlayerPositionWithOrientation?> =
        getCurrentLocationAndOrientationUseCase(Unit)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    /**
     * Now, map all location+orientation updates to be distinct on only the player position changing, and then also mapping orientation values out.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val innerCurrentLocation: StateFlow<PlayerPosition?> =
        innerCurrentLocationWithOrientation
            .distinctUntilChanged { old, new -> old?.position == new?.position }
            .mapLatest { it?.position }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * A state flow for the current player's complete state. This is where we'll package the User in use, their changing location (according to world socket state)
     * and any other game settings relevant to drawing them.
     */
    private val innerCurrentPlayer: StateFlow<CurrentPlayer?> =
        combine(
            currentCachedAccount,
            gameSettings,
            innerCurrentLocation
        ) { account, settings, playerPosition ->
            if(account != null && playerPosition != null && settings != null) {
                CurrentPlayer(
                    account,
                    settings,
                    playerPosition
                )
            } else {
                null
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Map the ongoing race UID to a Race instance. If null is returned, there is no race at all. Otherwise, if the race is cancelled or disqualified, this is
     * a not racing state. When a race is successfully finished, a leaderboard entry should be inserted into cache for that race. This should trigger the get
     * cached leaderboard entry flow to emit that value; which will qualify for the finished state.
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
            val requestGetRace = RequestGetRace(raceUid)
            return@flatMapLatest combine(
                getRaceUseCase(requestGetRace),
                getCachedLeaderboardEntryForRaceUseCase(requestGetRace)
            ) { race, leaderboardEntry ->
                return@combine when {
                    /**
                     * Race is null, there's no race.
                     */
                    race == null -> RaceState.NoRace

                    /**
                     * If race is marked as finished, and we have a leaderboard entry for the race, the race is finished.
                     */
                    race.isFinished && leaderboardEntry != null -> RaceState.RaceFinished(race, leaderboardEntry)

                    /**
                     * If race is cancelled or disqualified, we're not racing this instance but we should still display this outcome.
                     */
                    race.isCancelled || race.isDisqualified -> RaceState.NotRacing(race)

                    /**
                     * Otherwise, race is still ongoing.
                     */
                    else -> RaceState.Racing(race)
                }
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
                        // Loop from 4 down to 1, then 1 and 0 will actually be handled in UI state.
                        for(countdownSecond in 4 downTo 1) {
                            Timber.d("Emitting new countdown second: $countdownSecond")
                            emit(
                                RaceState.StartingRace(
                                    newRaceIntent.trackUid,
                                    newRaceIntent.vehicleUid,
                                    countdownSecond,
                                    newRaceIntent.countdownLocationWithOrientation
                                )
                            )
                            delay(1000)
                        }
                    }
                    is NewRaceIntentState.CancelRaceStart -> {
                        emit(RaceState.FailedStart(newRaceIntent.reason, newRaceIntent.resourceError))
                    }
                    else -> emit(RaceState.NoRace)
                }
            }
        }

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
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    /**
     * Map the current location and the resource for a track and its path to the most applicable start line state. This state communicates how appropriate the User's
     * current location is with respect to starting a new race on this track. There are three overall states; Perfect, the User is in a position to start the race;
     * Standby, the User has moved slightly away from the appropriate position to start racing, do not allow the race but do not leave race mode. MovedAway; the
     * Player has deviated sufficiently such that even being in race mode is no longer possible. Exit race mode.
     */
    private val startLineState: StateFlow<StartLineState> =
        combine(
            innerCurrentLocationWithOrientation,
            trackWithPathResource
        ) { locationWithOrientation, resource ->
            val location = locationWithOrientation?.position
            val orientationAngles = locationWithOrientation?.orientation

            // If location is null, or if resource is not success, or if data inside the success is null, we'll return standby.
            if(location == null || orientationAngles == null || resource.status != Resource.Status.SUCCESS || resource.data == null) {
                return@combine StartLineState.Inconclusive
            }
            // Now, get the track.
            val track: Track = resource.data!!.track
            // Get the distance to the start point here.
            val distanceToStart: Float = track.distanceToStartPointFor(location.latitude, location.longitude)
            // Determine whether orientation is correct.
            val isOrientationCorrect: Boolean = track.isOrientationCorrectFor(orientationAngles.rotation)
            Timber.d("Checking location: distance: ${distanceToStart}m, orientation: $isOrientationCorrect")
            return@combine when {
                /**
                 * When distance to start is within 10m and orientation is correct, we are in perfect position.
                 */
                distanceToStart <= 10f && isOrientationCorrect ->
                    StartLineState.Perfect(locationWithOrientation)

                /**
                 * When distance to start is greater than 30m, we have moved away.
                 */
                distanceToStart > 30f ->
                    StartLineState.MovedAway(locationWithOrientation)

                /**
                 * Otherwise, at this point; distance to start is within the range of 10.1 to 30 and irrespective of whether orientation is
                 * correct, we are in standby.
                 */
                else -> StartLineState.Standby(locationWithOrientation)
            }
        }.distinctUntilChanged { oldState, newState ->
            // Attach a distinct until changed that will function on the actual types of each state instead of the contents thereof,
            // this will stop race countdowns being interrupted for duplicate 'Perfect' start line state.
            oldState::class.java == newState::class.java
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StartLineState.Inconclusive)

    /**
     * A merged flow of both the race state as taken from cache and a race state as taken from mutable intent to begin a new race.
     */
    private val raceState: StateFlow<RaceState> =
        merge(
            raceStateFromCache,
            newRaceState
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RaceState.NoRace)

    /**
     * Now, combine the race state, track+path, the start line indicator and the user's current vehicles to a single flow that indicates the state
     * of the world map race.
     *
     * TODO: this should react to the case where a User has no vehicles. This should cause a supplementary state which requires the User to first create a new Vehicle. We can't just check
     * TODO: for an empty vehicles list here, because cache might be empty if query hasn't been completed before. One option is to change repository method for querying our vehicles to
     * TODO: flowQueryNetworkAndCache which will not observe the database. We can then take an empty list as indication there's really no vehicles. This however has the disadvantage that
     * TODO: changes to those vehicles will not be immediately reflected.
     */
    private val innerWorldMapRaceUiState: SharedFlow<WorldMapRaceUiState> =
        combineTransform(
            raceState,
            trackWithPathResource,
            startLineState,
            ourVehicles
        ) { race, resource, startLine, vehiclesResource ->
            when {
                /**
                 * Consider new race intent immediately. If it is in start countdown, but the User no longer qualifies for a new race, emit an early disqualification to
                 * the mutable new race intent, then return the loading state from here.
                 */
                race is RaceState.StartingRace && (startLine !is StartLineState.Perfect || resource.status != Resource.Status.SUCCESS || resource.data == null) -> {
                    emit(WorldMapRaceUiState.Loading)
                    // Disqualify the User by emitting an early disqualification state to our new race intent, which should cause the EarlyDisqualification branch to be triggered.
                    mutableNewRaceIntentState.value = NewRaceIntentState.CancelRaceStart(
                        CANCEL_FALSE_START
                    )
                }

                /**
                 * If start line state is inconclusive, or either track/our vehicles resources are loading, or vehicles have loaded but there are 0 in the list, emit loading state.
                 */
                startLine is StartLineState.Inconclusive || resource.status == Resource.Status.LOADING || vehiclesResource.status == Resource.Status.LOADING ||
                        (vehiclesResource.status == Resource.Status.SUCCESS && vehiclesResource.data!!.vehicles.isEmpty()) ->
                    emit(WorldMapRaceUiState.Loading)

                /**
                 * If track/path resource has failed, and has an ERROR status, we'll emit the load failed state.
                 */
                resource.status == Resource.Status.ERROR ->
                    emit(WorldMapRaceUiState.LoadFailed(resource.resourceError!!))

                /**
                 * If vehicles resource has failed, and has an ERROR status, we'll emit the load failed state.
                 */
                resource.status == Resource.Status.ERROR ->
                    emit(WorldMapRaceUiState.LoadFailed(vehiclesResource.resourceError!!))

                /**
                 * If resource has a null path, emit an error that communicates this track does not have a path.
                 */
                resource.data!!.path == null ->
                    emit(WorldMapRaceUiState.LoadFailed(ResourceError.GeneralError(NO_TRACK_PATH, NoTrackPathException())))

                /**
                 * If new race intent is early disqualification, emit a disqualification UI state.
                 */
                race is RaceState.FailedStart -> {
                    emit(
                        WorldMapRaceUiState.RaceStartFailed(
                            race.reasonString,
                            race.resourceError
                        )
                    )
                }

                /**
                 * If we are starting a race, all emissions before 1 will just be emitted as normal countdowns. At second count 1, we want to create the race on the server, receiving back
                 * a response and emitting the second 1. If the response is successful, we'll then emit second 4 (display second GO) from here and immediately try emit the selected race.
                 * At this point, the User can actually move without being disqualified.
                 */
                race is RaceState.StartingRace -> {
                    try {
                        if(race.currentSecond == 1) {
                            // We require a current location to continue. Disqualify the countdown if this does not exist.
                            val location: PlayerPosition = innerCurrentLocation.value
                                ?: throw NoLocationException()
                            // Current second is 1, just before GO. This is where we'll send for a new race and emit the result to the mutable state flow for ongoing race UID.
                            Timber.d("Requesting to begin the new race NOW!")
                            // Create a new request to start a race, with the desired track and vehicle.
                            val requestStartRace = RequestStartRace(
                                race.trackUid,
                                race.vehicleUid,
                                RequestPlayerUpdate(location),
                                RequestPlayerUpdate(race.countdownLocationWithOrientation.position)
                            )
                            // Now execute the actual request in a separate coroutine.
                            val startRaceResult = sendStartRaceRequestUseCase(requestStartRace)
                            if(!startRaceResult.isStarted) {
                                throw StartRaceFailedException(startRaceResult.socketError!!)
                            }
                            // Now, immediately emit second 1.
                            emit(
                                WorldMapRaceUiState.CountingDown(
                                    1,
                                    race.countdownLocationWithOrientation,
                                    resource.data!!.track,
                                    resource.data!!.path!!
                                )
                            )
                            // Delay for 1000 ms.
                            delay(1000)
                            // Now, emit second 0 (display second GO) and the race UID at the same time.
                            emit(
                                WorldMapRaceUiState.CountingDown(
                                    0,
                                    race.countdownLocationWithOrientation,
                                    resource.data!!.track,
                                    resource.data!!.path!!
                                )
                            )
                            // Now try emit the ongoing race's UID, this will cause the race UID to be mapped to the actual Race instance.
                            mutableOngoingRaceUid.tryEmit(startRaceResult.race!!.raceUid)
                        } else {
                            // Always emit the current second as a countdown update.
                            emit(
                                WorldMapRaceUiState.CountingDown(
                                    race.currentSecond,
                                    race.countdownLocationWithOrientation,
                                    resource.data!!.track,
                                    resource.data!!.path!!
                                )
                            )
                        }
                    } catch(nle: NoLocationException) {
                        Timber.d("Cancelling countdown because no current location when race being created!")
                        emit(WorldMapRaceUiState.Loading)
                        // Emit a cancel race state to our mutable new race intent with reason no location.
                        mutableNewRaceIntentState.value = NewRaceIntentState.CancelRaceStart(
                            CANCEL_RACE_REASON_NO_LOCATION
                        )
                    } catch(srfe: StartRaceFailedException) {
                        Timber.d("Cancelling countdown server informed us race could not be started!")
                        emit(WorldMapRaceUiState.Loading)
                        // Emit a cancel race state to our mutable new race intent with reason server refused and pass the resource error along.
                        mutableNewRaceIntentState.value = NewRaceIntentState.CancelRaceStart(
                            CANCEL_RACE_SERVER_REFUSED,
                            srfe.socketError
                        )
                    } catch(e: Exception) {
                        Timber.e(e)
                        // TODO: For all other exceptions, throw not impl
                        throw NotImplementedError()
                    }
                }

                /**
                 * There is no race ongoing or starting. Simply present the start line state. This is how we will also request that the User select a vehicle to use
                 * from the given vehicles list.
                 */
                race is RaceState.NoRace ->
                    emit(
                        WorldMapRaceUiState.OnStartLine(
                            vehiclesResource.data!!.vehicles,
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
                 * If we have a race finished state, this means we have completed a race.
                 */
                race is RaceState.RaceFinished ->
                    WorldMapRaceUiState.Finished(
                        race.race,
                        race.leaderboardEntry,
                        resource.data!!.track,
                        resource.data!!.path!!
                    )

                /**
                 * If we have a not racing state, this either means we have a cancelled race, or a disqualified race.
                 */
                race is RaceState.NotRacing ->
                    emit(
                        when {
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
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    /**
     * Publicise a merging of the manual and automatic UI flows.
     */
    val worldMapRaceUiState: StateFlow<WorldMapRaceUiState> =
        merge(
            innerWorldMapRaceUiState,
            mutableWorldMapRaceUiState
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorldMapRaceUiState.Loading)

    /**
     * Publicise the current player.
     */
    val currentPlayer: StateFlow<CurrentPlayer?> =
        innerCurrentPlayer

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
        chosenVehicle: Vehicle,
        track: Track,
        countdownPositionWithOrientation: PlayerPositionWithOrientation
    ) {
        viewModelScope.launch(ioDispatcher) {
            // Emit a new countdown state to the mutable start countdown flow.
            mutableNewRaceIntentState.emit(
                NewRaceIntentState.NewCountdown(
                    track.trackUid,
                    chosenVehicle.vehicleUid,
                    countdownPositionWithOrientation
                )
            )
        }
    }

    /**
     * Cancel the ongoing race. If the countdown has only started, this will simply abort the countdown. Otherwise, this will cancel the race with the server.
     */
    fun cancelRace() {
        viewModelScope.launch(ioDispatcher) {
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
                    mutableNewRaceIntentState.value = NewRaceIntentState.CancelRaceStart(
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
     * If a race has been disqualified, cancelled or otherwise interrupted such that it has been stopped, the UI will show the reason and potential corrective action for
     * whatever the issue was. When the Player has accepted the issue, or resolved the issue, this function will reset the error'd UI back to idle, which will then trigger
     * the NoRace state.
     */
    fun resetRaceIntent() {
        mutableNewRaceIntentState.tryEmit(
            NewRaceIntentState.Idle
        )
    }

    companion object {
        const val CANCEL_RACE_REASON_NO_LOCATION = "no-location"
        const val CANCEL_RACE_SERVER_REFUSED = "server-refused"
        const val CANCEL_FALSE_START = "start-point-not-perfect"
        const val CANCELLED_BY_USER = "cancelled-by-user"
    }
}
