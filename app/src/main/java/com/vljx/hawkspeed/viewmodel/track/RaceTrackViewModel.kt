package com.vljx.hawkspeed.viewmodel.track

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.interactor.track.GetTrackPathUseCase
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.track.TrackPoint
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import com.vljx.hawkspeed.models.track.RaceCountdownState
import com.vljx.hawkspeed.models.track.RaceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RaceTrackViewModel @Inject constructor(
    private val getTrackPathUseCase: GetTrackPathUseCase
): ViewModel() {
    /**
     * A shared flow for the currently selected track.
     */
    private val mutableSelectedTrack: MutableSharedFlow<Track> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * The state flow for the device's latest location.
     */
    private val mutableLatestLocation: MutableStateFlow<Location?> = MutableStateFlow(null)

    /**
     * A state flow for the race's current state.
     */
    private val mutableRaceState: MutableStateFlow<RaceState> = MutableStateFlow(RaceState.Preparing)

    /**
     * A flow that will map the selected track to a flow that will query the latest path for that track.
     */
    private val selectedTrackPathResource: Flow<Resource<TrackPath>> =
        mutableSelectedTrack.flatMapLatest { selectedTrack ->
            getTrackPathUseCase(
                GetTrackPathRequest(selectedTrack.trackUid)
            )
        }

    /**
     * The actual race state.
     */
    val raceState: StateFlow<RaceState> =
        mutableRaceState

    /**
     * The race countdown's state.
     * This will transform the latest race state. If the state is countdown started, the transform block will run code that will emit each value of the countdown,
     * until GO is reached. At which point, the racing state is emitted to the overall race state. If race state is anything except CountdownStarted, the transform
     * will simply emit the Idle state.
     */
    val raceCountdownState: Flow<RaceCountdownState> =
        mutableRaceState.transformLatest { raceState ->
            if(raceState is RaceState.CountdownStarted) {
                // If the race state is countdown started, play out a countdown.
                // Start by emitting the GetReady state.
                emit(RaceCountdownState.GetReady)
                // Wait 1.5 seconds.
                delay(1500)
                // Now, loop 3 times, each time, emitting an OnCount with each number and delaying for that second.
                for(x in 3 downTo 1) {
                    emit(RaceCountdownState.OnCount(x))
                    delay(1000)
                }
                // Finally, emit GO with the location we started countdown at.
                emit(RaceCountdownState.Go)
                mutableRaceState.tryEmit(RaceState.Racing(raceState.trackUid, raceState.startedAt))
            } else {
                // Otherwise, just emit the idle state.
                emit(RaceCountdownState.Idle)
            }
        }

    /**
     * A flow for the selected track.
     */
    val selectedTrack: Flow<Track> =
        mutableSelectedTrack.distinctUntilChanged()

    /**
     * A flow that will transform the selected track path resource to the track path, but only when the path is successfully queried.
     */
    val selectedTrackPath: Flow<TrackPath> =
        selectedTrackPathResource.transformLatest { trackPathResource ->
            if(trackPathResource.status == Resource.Status.SUCCESS) {
                // On success, we'll emit the track path itself.
                emit(trackPathResource.data!!)
            }
        }

    /**
     * A state flow for whether the latest location is within an acceptable distance to the selected track's start point.
     */
    val isOnStartLine: StateFlow<Boolean> =
        combine(
            mutableLatestLocation,
            selectedTrack
        ) { location, track ->
            // TODO: a more advanced check for this. For now, we will simply check whether distance is <= 15m or so.
            if(location != null) {
                // Compute distance between latest location and start point.
                val distanceBetweenArray = FloatArray(5)
                Location.distanceBetween(
                    location.latitude,
                    location.longitude,
                    track.startPoint.latitude,
                    track.startPoint.longitude,
                    distanceBetweenArray
                )
                // First index is the distance, in meters. Return true if this value is equal to or less than 15.
                return@combine distanceBetweenArray[0] <= 15.0f
            } else {
                // If location is null, immediately return false.
                return@combine false
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * A state flow for whether we can start the race, that is, the countdown.
     * We can start the race as long as we are in a preparing state, and we are currently on the start line.
     */
    val canStartRace: StateFlow<Boolean> =
        combine(
            mutableRaceState,
            isOnStartLine
        ) { state, onStartLine ->
            state == RaceState.Preparing && onStartLine
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * A state flow for whether we can end the race (cancel.)
     * We can cancel the race as long as we are in either a countdown started or racing state.
     */
    val canCancelRace: StateFlow<Boolean> =
        mutableRaceState.map { state ->
            state is RaceState.CountdownStarted || state is RaceState.Racing
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Start the race at the current location.
     * The given location will be preserved as the lined up position, deviation from this location during the countdown will incur disqualification.
     * This function is a suspend function because it should be called on a view lifecycle; so we are sure the race start will not continue if the view is destroyed.
     */
    suspend fun startRace(track: Track, location: Location) {
        if(!canStartRace.value) {
            throw NotImplementedError("Failed to startRace! We are not able to just yet, even though view model was asked to.")
        }
        // TODO: some other things to denote the start of the race.
        // Emit the countdown started state, alongside this location. This will trigger the countdown flow, which will trigger the actual race.
        mutableRaceState.tryEmit(RaceState.CountdownStarted(track.trackUid, location))
    }

    /**
     * Cancel the race early.
     */
    fun cancelRace() {
        if(!canCancelRace.value) {
            throw NotImplementedError("Failed to cancelRace! We are not able to just yet, even though view model was asked to.")
        }
        // TODO: some other things to denote cancellation.
        // Emit the cancelled race state.
        mutableRaceState.tryEmit(RaceState.Cancelled)
    }

    /**
     * Inform the User they have been disqualified and enact the changes required.
     * The server has already decided this is the course of action being taken at this point.
     */
    fun disqualifyRace() {
        //
    }

    /**
     * Select the track we wish to race.
     */
    fun selectTrack(track: Track) {
        mutableSelectedTrack.tryEmit(track)
    }

    /**
     * Update the device's current location.
     */
    fun updateLocation(location: Location) {
        mutableLatestLocation.tryEmit(location)
    }
}