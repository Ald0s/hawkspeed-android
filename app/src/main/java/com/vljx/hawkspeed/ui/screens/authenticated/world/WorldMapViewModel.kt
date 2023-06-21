package com.vljx.hawkspeed.ui.screens.authenticated.world

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.VisibleRegion
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.states.socket.WorldSocketState
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.WorldObjects
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestJoinWorld
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestLeaveWorld
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestViewportUpdate
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.world.RequestGetWorldObjects
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationUseCase
import com.vljx.hawkspeed.domain.usecase.socket.GetWorldSocketStateUseCase
import com.vljx.hawkspeed.domain.usecase.socket.RequestJoinWorldUseCase
import com.vljx.hawkspeed.domain.usecase.socket.RequestLeaveWorldUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendViewportUpdateUseCase
import com.vljx.hawkspeed.domain.usecase.track.GetTrackWithPathUseCase
import com.vljx.hawkspeed.domain.usecase.world.GetWorldObjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorldMapViewModel @Inject constructor(
    getCurrentLocationUseCase: GetCurrentLocationUseCase,
    getWorldSocketStateUseCase: GetWorldSocketStateUseCase,

    private val getWorldObjectsUseCase: GetWorldObjectsUseCase,
    private val getTrackWithPathUseCase: GetTrackWithPathUseCase,

    private val requestJoinWorldUseCase: RequestJoinWorldUseCase,
    private val requestLeaveWorldUseCase: RequestLeaveWorldUseCase,
    private val sendViewportUpdateUseCase: SendViewportUpdateUseCase
): ViewModel() {
    /**
     * Get the current location from world socket session.
     */
    private val currentLocation: StateFlow<PlayerPosition?> =
        getCurrentLocationUseCase(Unit)

    /**
     * Get the current world socket session's state.
     */
    private val currentSocketSessionState: SharedFlow<WorldSocketState> =
        getWorldSocketStateUseCase(Unit)

    /**
     * The current request for world objects. Emitting to this state flow will cause a revision of world objects currently
     * displayed on the map.
     */
    private val mutableCurrentRequestGetWorldObjects: MutableStateFlow<RequestGetWorldObjects> = MutableStateFlow(RequestGetWorldObjects())

    /**
     * A flow that will collect all game settings, this includes things like; whether the Player is allowing the game, the server's connection info and
     * any identity data we'll need to actually connect.
     */
    // TODO: this should be retrieved from cache.
    private val gameSettings: StateFlow<GameSettings?> =
        MutableStateFlow(
            GameSettings(
                true,
                "ENTRY TOKEN",
                "http://192.168.0.44:5000"
            )
        )

    /**
     * Will only allow emissions if the old is no longer null; which means this flow will not emit anything except the first
     * non-null location after a null location.
     */
    private val currentLocationDistinctOnNull: StateFlow<PlayerPosition?> =
        currentLocation.distinctUntilChanged { old, new ->
            old != null
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

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
     * Flat map the latest desired world objects configuration to a new flow from cache to retrieve the desired world objects.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val worldObjectsFlow: Flow<WorldObjects> =
        mutableCurrentRequestGetWorldObjects.flatMapLatest { value: RequestGetWorldObjects ->
            getWorldObjectsUseCase(value)
        }

    /**
     * Map each emission from cache to the relevant world objects UI state. Notice that where lists are used, each must be transformed into mutable state lists to
     * ensure compose will recompose when the list contents changes.
     */
    val worldObjectsUiState: StateFlow<WorldObjectsUiState> =
        worldObjectsFlow.map { worldObjects ->
            WorldObjectsUiState.GotWorldObjects(
                worldObjects.tracks.toMutableStateList()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WorldObjectsUiState.Loading)

    /**
     * Publicise both states.
     */
    val locationPermissionState: SharedFlow<LocationPermissionState> =
        mutableLocationPermissionState

    val locationSettingsState: SharedFlow<LocationSettingsState> =
        mutableLocationSettingsState

    /**
     * Combine the current location, location permissions and location settings states and map these to the most appropriate world map ui state. The first emission
     * from this state flow will only be considered when all sources emit at least one. If settings are not appropriate, fail. If location permissions are either
     * fully granted or approximate, this is a map loaded condition. If the location is null, emit a location failure to trigger a start command to the service.
     * Finally, the socket connection to the server will be monitored and reported as appropriate.
     */
    val worldMapUiState: StateFlow<WorldMapUiState> =
        combine(
            gameSettings,
            locationPermissionState.distinctUntilChanged(),
            locationSettingsState.distinctUntilChanged(),
            currentLocationDistinctOnNull,
            currentSocketSessionState
        ) { settings, permissionState, settingsState, location, socketState ->
            return@combine when {
                (settings == null) || (!settings.canConnectGame || !settings.isServerAvailable) -> WorldMapUiState.GameSettingsFailure(settings)
                settingsState !is LocationSettingsState.Appropriate || (permissionState is LocationPermissionState.NoneGranted) ->
                    WorldMapUiState.PermissionOrSettingsFailure(permissionState, settingsState)
                location == null -> WorldMapUiState.NoLocation(null)
                socketState is WorldSocketState.Disconnected -> {
                    if(socketState.resourceError != null) {
                        // This is a connection error.
                        WorldMapUiState.ConnectionFailure(socketState.resourceError)
                    } else {
                        // Simply not connected.
                        WorldMapUiState.NotConnected(
                            settings,
                            location
                        )
                    }
                }
                socketState is WorldSocketState.Connecting -> WorldMapUiState.Loading
                else -> WorldMapUiState.WorldMapLoaded(
                    (socketState as WorldSocketState.Connected).playerUid,
                    settings,
                    location,
                    permissionState is LocationPermissionState.OnlyCoarseGranted
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WorldMapUiState.Loading)

    /**
     * Ensure the socket client is connected to the server, given the game settings and location. If the server is already connected or connecting,
     * this function will not explicitly cause any reconnection until the next connection cycle.
     */
    fun joinWorld(settings: GameSettings, location: PlayerPosition) {
        requestJoinWorldUseCase(
            RequestJoinWorld(settings, location)
        )
    }

    /**
     * Ensure the socket client is not connected to the server.
     */
    fun leaveWorld() {
        // TODO: we can provide reason string here.
        requestLeaveWorldUseCase(
            RequestLeaveWorld()
        )
    }

    /**
     * Update the viewport for the device.
     */
    fun updateViewport(visibleRegion: VisibleRegion, zoom: Float) {
        viewModelScope.launch {
            sendViewportUpdateUseCase(
                RequestViewportUpdate(
                    visibleRegion.latLngBounds.southwest.longitude,
                    visibleRegion.latLngBounds.southwest.latitude,
                    visibleRegion.latLngBounds.northeast.longitude,
                    visibleRegion.latLngBounds.northeast.latitude,
                    zoom
                )
            )
        }
    }

    /**
     * Update the status of location permissions; coarse and fine.
     */
    fun updateLocationPermission(
        coarseAccessGranted: Boolean,
        fineAccessGranted: Boolean
    ) {
        mutableLocationPermissionState.tryEmit(
            when {
                coarseAccessGranted && fineAccessGranted -> LocationPermissionState.AllGranted
                coarseAccessGranted -> LocationPermissionState.OnlyCoarseGranted
                else -> LocationPermissionState.NoneGranted
            }
        )
    }

    /**
     * Update the status of location settings.
     */
    fun updateLocationSettings(appropriate: Boolean) {
        mutableLocationSettingsState.tryEmit(
            when(appropriate) {
                true -> LocationSettingsState.Appropriate
                else -> LocationSettingsState.NotAppropriate
            }
        )
    }

    /**
     * Change the settings for which world objects to display.
     */
    fun setRequestGetWorldObjects(requestGetWorldObjects: RequestGetWorldObjects) {
        mutableCurrentRequestGetWorldObjects.tryEmit(requestGetWorldObjects)
    }

    /**
     * Perform a download of the given track, to update its details stored in cache, but also to download its track and cache it.
     * For that reason, this function will simply collect the flow right here on the view model scope.
     */
    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            getTrackWithPathUseCase(
                RequestGetTrackWithPath(track.trackUid)
            ).collect()
        }
    }
}