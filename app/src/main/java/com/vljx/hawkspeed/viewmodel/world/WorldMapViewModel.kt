package com.vljx.hawkspeed.viewmodel.world

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.data.socket.WorldSocketSession
import com.vljx.hawkspeed.data.socket.WorldSocketState
import com.vljx.hawkspeed.domain.interactor.track.GetTrackPathUseCase
import com.vljx.hawkspeed.domain.interactor.track.GetTracksUseCase
import com.vljx.hawkspeed.domain.interactor.track.GetTracksWithPathsUseCase
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorldMapViewModel @Inject constructor(
    private val worldSocketSession: WorldSocketSession,

    private val getTracksUseCase: GetTracksUseCase,
    private val getTrackPathUseCase: GetTrackPathUseCase,
    private val getTracksWithPathsUseCase: GetTracksWithPathsUseCase
): ViewModel() {
    //private val mutableIsLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)

    /**
     * A stateflow for whether the connection to the world is loading.
     * This simply maps the world socket state from the socket session.
     */
    val isLoading: StateFlow<Boolean> =
        worldSocketSession.worldSocketState.map { worldSocketState ->
            worldSocketState is WorldSocketState.Connecting
        }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /**
     * A flow for a Track, the start point of which the device is on such that they qualify to begin racing that track.
     * This is done by combining a query to get all tracks from cache, as well as the most recent location set in the world socket session, then checking
     * that the device is close enough and oriented to the right bearing.
     *
     * TODO: complete this with a bearing check.
     */
    val canRaceOn: Flow<Track?> =
        combine(
            getTracksUseCase(Unit),
            worldSocketSession.currentLocation
        ) { tracks, location ->
            if(location != null) {
                // Now, iterate all tracks, and check that the distance between the track and the location is at most 10 meters.
                tracks.forEach { track ->
                    // Get the distance.
                    val distanceResultArray = FloatArray(5)
                    Location.distanceBetween(
                        track.startPoint.latitude,
                        track.startPoint.longitude,
                        location.latitude,
                        location.longitude,
                        distanceResultArray
                    )
                    // If the distance is equal to, less than 10, return the track.
                    if(distanceResultArray[0] <= 10f) {
                        return@combine track
                    }
                }
            }
            return@combine null
        }.distinctUntilChanged()

    /**
     *
     */
    val tracksWithPaths: Flow<List<TrackWithPath>> =
        getTracksWithPathsUseCase(Unit)

    /**
     *
     */
    //val isLoading: StateFlow<Boolean> =
    //    mutableIsLoading

    //fun setLoading(loading: Boolean) {
    //    mutableIsLoading.tryEmit(loading)
    //}

    fun getTrackPath(track: Track) {
        viewModelScope.launch {
            getTrackPathUseCase(GetTrackPathRequest(track.trackUid)).collect {

            }
        }
    }
}