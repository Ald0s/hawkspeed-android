package com.vljx.hawkspeed.viewmodel.world

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.data.socket.WorldSocketSession
import com.vljx.hawkspeed.data.socket.WorldSocketState
import com.vljx.hawkspeed.domain.authentication.AuthenticationSession
import com.vljx.hawkspeed.domain.authentication.AuthenticationState
import com.vljx.hawkspeed.domain.interactor.track.GetTrackPathUseCase
import com.vljx.hawkspeed.domain.interactor.track.GetTracksUseCase
import com.vljx.hawkspeed.domain.interactor.track.GetTracksWithPathsUseCase
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import com.vljx.hawkspeed.view.world.LocationPermissionState
import com.vljx.hawkspeed.view.world.LocationSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorldMapViewModel @Inject constructor(
    private val authenticationSession: AuthenticationSession,
    private val worldSocketSession: WorldSocketSession,

    private val getTracksUseCase: GetTracksUseCase,
    private val getTrackPathUseCase: GetTrackPathUseCase,
    private val getTracksWithPathsUseCase: GetTracksWithPathsUseCase
): ViewModel() {
    /**
     * A mutable state flow for the current loading status, as a string.
     */
    private val mutableLoadingStatus: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * A shared flow for the latest location permission state, we will replay a single value.
     */
    private val mutableLocationPermissionState: MutableSharedFlow<LocationPermissionState> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * A shared flow for the latest location settings state, we will replay a single value.
     */
    private val mutableLocationSettingsState: MutableSharedFlow<LocationSettingsState> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * A public location permission flow, this will add distinct until changed.
     */
    val locationPermissionState: Flow<LocationPermissionState> =
        mutableLocationPermissionState.distinctUntilChanged()

    /**
     * A public location settings flow, this will add distinct until changed.
     */
    val locationSettingsState: Flow<LocationSettingsState> =
        mutableLocationSettingsState.distinctUntilChanged()

    /**
     * A state flow boolean for when the loading overlay should be shown, and the map itself should be hidden.
     * This overlay should be shown as long as settings are not appropriate, permission is not/partially granted or the socket session is not actively connected.
     */
    val shouldShowLoadingOverlay: StateFlow<Boolean> =
        combine(
            worldSocketSession.worldSocketState,
            locationPermissionState,
            locationSettingsState
        ) { socketState, permissionState, settingsState ->
            return@combine socketState !is WorldSocketState.Connected
                    || permissionState !is LocationPermissionState.AllGranted
                    || settingsState !is LocationSettingsState.Appropriate
        }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /**
     * A flow for the world socket state in the world socket session.
     */
    val worldSocketState: Flow<WorldSocketState> =
        worldSocketSession.worldSocketState

    /**
     * A state flow for the loading status string itself.
     * TODO: place this text into a TextView under the progress bar.
     */
    val loadingStatus: StateFlow<String?> =
        mutableLoadingStatus

    /**
     * A state flow for whether there is a loading status to display. This status appears on the loading overlay.
     * TODO: place this boolean into the enabled attribute for the TextView mentioned above.
     */
     val hasLoadingStatus: StateFlow<Boolean> =
        mutableLoadingStatus.map { status ->
            status != null
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * A state flow that will indicate whether the Player can create new tracks.
     * TODO: place this boolean into a visibility to show the button if we are allowed, or hide it if not.
     */
    val canCreateNewTracks: StateFlow<Boolean> =
        authenticationSession.authenticationState.map { value: AuthenticationState ->
            return@map when(value) {
                is AuthenticationState.Authenticated -> {
                    // If it is authenticated, return whether we can create tracks.
                    return@map value.canCreateTracks
                }
                else -> false
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * A flow for a Track which the Player is standing within race proximity to. This determination is done by combining a query to get all tracks from
     * cache, as well as the most recent location set in the world socket session, then checking that the device is close enough and oriented to the right bearing.
     */
    val trackCanBeRaced: Flow<Track?> =
        combine(
            getTracksUseCase(Unit),
            worldSocketSession.currentLocation
        ) { tracks, location ->
            if(location != null) {
                // Now, iterate all tracks, and check that the Player can race this track, at this location.
                tracks.forEach { track ->
                    if(track.canBeRacedBy(location.latitude, location.longitude, location.bearing)) {
                        return@combine track
                    }
                }
            }
            return@combine null
        }.distinctUntilChanged()

    /**
     * A flow for a list of all tracks that are cached, alongside their paths, if they too have been cached. Use this flow to draw all
     * tracks to the map.
     */
    val tracksWithPaths: Flow<List<TrackWithPath>> =
        getTracksWithPathsUseCase(Unit)

    /**
     * Perform a query for the given track's full path. This will be cached.
     */
    fun getTrackPath(track: Track) {
        viewModelScope.launch {
            getTrackPathUseCase(
                GetTrackPathRequest(track.trackUid)
            ).collect()
        }
    }

    /**
     * Update the location permission currently afforded to the device.
     */
    fun locationPermissionsUpdated(coarseAccessGranted: Boolean, fineAccessGranted: Boolean) {
        mutableLocationPermissionState.tryEmit(
            when {
                coarseAccessGranted && fineAccessGranted -> LocationPermissionState.AllGranted
                coarseAccessGranted -> LocationPermissionState.OnlyCoarseGranted
                else -> LocationPermissionState.NoneGranted
            }
        )
    }

    /**
     * Update whether location settings are currently appropriate.
     */
    fun locationSettingsAppropriate(areAppropriate: Boolean) {
        mutableLocationSettingsState.tryEmit(
            when {
                areAppropriate -> LocationSettingsState.Appropriate
                else -> LocationSettingsState.NotAppropriate
            }
        )
    }
}