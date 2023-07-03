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
     * A data class for collapsing the Android location related states into a single entity.
     */
    data class LocationAccess(
        val locationPermissionState: LocationPermissionState,
        val locationSettingsState: LocationSettingsState
    )

    /**
     * Get the current location from world socket session.
     */
    private val innerCurrentLocation: StateFlow<PlayerPosition?> =
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
        innerCurrentLocation.distinctUntilChanged { old, new ->
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
     * Combine the location permission and settings states into a location access entity.
     */
    private val locationAccessStates: Flow<LocationAccess> =
        combine(
            mutableLocationPermissionState
                .distinctUntilChanged(),
            mutableLocationSettingsState
                .distinctUntilChanged()
        ) { permissionState, settingsState ->
            LocationAccess(
                permissionState,
                settingsState
            )
        }

    /**
     * A mutable state flow for the current world action state.
     */
    private val mutableWorldActionState: MutableStateFlow<WorldActionState> = MutableStateFlow(WorldActionState.StandardMode)

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
     * Combine all parameters required in determining what UI state the world should currently be displaying.
     *
     * TODO: world map action states that are non-standard such as race mode and record track mode should not work with approximate only location. This check ideally should
     * TODO: be performed much sooner, such as disabling related buttons that trigger race mode/record track mode for approximate only states. But its also a good idea to
     * TODO: support a mini check thereof in this flow; perhaps like an Error state that displays a dialog with grayed boundaries, that lets the Player know this action is
     * TODO: not supported until fine location access given, and with a dialog button that emits standard mode to be used on click.
     */
    val worldMapUiState: StateFlow<WorldMapUiState> =
        combine(
            gameSettings,
            mutableWorldActionState,
            locationAccessStates,
            currentLocationDistinctOnNull,
            currentSocketSessionState
        ) { settings, worldAction, locationAccess, location, socketState ->
            val permissionState = locationAccess.locationPermissionState
            val settingsState = locationAccess.locationSettingsState

            return@combine when {
                /**
                 * Fail with game settings failure if either the User has decided against connecting to game server (canConnectGame is false) or if server has reported
                 * that the game server is down (isServerAvailable is false).
                 */
                (settings == null) || (!settings.canConnectGame || !settings.isServerAvailable) ->
                    WorldMapUiState.GameSettingsFailure(settings)

                /**
                 * Fail with permission/settings failure if location settings are not appropriate, or location permission granted is None granted. Allow coarse location
                 * access at this point.
                 */
                settingsState !is LocationSettingsState.Appropriate || (permissionState is LocationPermissionState.NoneGranted) ->
                    WorldMapUiState.PermissionOrSettingsFailure(permissionState, settingsState)

                /**
                 * Fail with no location failure if there is no current location. This will prompt world service to start receiving location updates if not already.
                 */
                location == null ->
                    WorldMapUiState.NoLocation(null)

                /**
                 * If socket state is disconnected and resource error is not null, or socket state is connection refused, emit a connection failure error which will deal
                 * with whatever the problem was.
                 */
                socketState is WorldSocketState.Disconnected && socketState.resourceError != null ->
                    WorldMapUiState.ConnectionFailure(socketState.resourceError)
                socketState is WorldSocketState.ConnectionRefused ->
                    WorldMapUiState.ConnectionFailure(socketState.resourceError)

                /**
                 * Otherwise, if socket state is disconnected, we're simply not yet connected.
                 */
                socketState is WorldSocketState.Disconnected ->
                    WorldMapUiState.NotConnected(
                        settings,
                        location
                    )

                /**
                 * Emit the loading state if we are currently connecting to the server.
                 */
                socketState is WorldSocketState.Connecting ->
                    WorldMapUiState.Loading

                /**
                 * If world action requests standard mode, emit the standard mode UI state with all applicable arguments. Coarse location access is allowed
                 * at this point.
                 */
                worldAction is WorldActionState.StandardMode ->
                    WorldMapUiState.WorldMapLoadedStandardMode(
                    (socketState as WorldSocketState.Connected).playerUid,
                    settings,
                    location,
                    permissionState is LocationPermissionState.OnlyCoarseGranted
                )

                /**
                 * If world action is either record track or race mode, and we only have coarse location access, fail with the error world state communicating that
                 * full access is required to perform this action.
                 */
                (permissionState is LocationPermissionState.OnlyCoarseGranted) && (worldAction is WorldActionState.RecordTrackMode || worldAction is WorldActionState.RaceMode) ->
                    throw NotImplementedError("only coarse granted, world action requires full permission is NOT HANDLED. Return Non standard mode failure") // TODO: implement this please.
                    /*WorldMapUiState.NonStandardModeFailure(
                        worldAction,
                        "UNKOWN"
                    )*/

                /**
                 * If world action is record track mode, we'll emit the appropriate UI state, since we have all arguments satisfied.
                 */
                worldAction is WorldActionState.RecordTrackMode ->
                    WorldMapUiState.WorldMapLoadedRecordTrackMode(
                        (socketState as WorldSocketState.Connected).playerUid,
                        settings,
                        location
                    )

                /**
                 * If world action is race mode, we'll emit the appropriate UI state, since we have all arguments satisfied.
                 */
                worldAction is WorldActionState.RaceMode ->
                    WorldMapUiState.WorldMapLoadedRaceMode(
                        (socketState as WorldSocketState.Connected).playerUid,
                        settings,
                        location,
                        worldAction.trackUid
                    )

                /**
                 * The default case, unhandled for now.
                 * TODO: handle this.
                 */
                else -> throw NotImplementedError("Failed to emit proper action for worldMapUiState. A case was not handled.")
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WorldMapUiState.Loading)

    /**
     * Publicise the current location state flow.
     */
    val currentLocation: StateFlow<PlayerPosition?> =
        innerCurrentLocation

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

    /**
     * Enter race mode for a particular track's UID.
     */
    fun enterRaceMode(trackUid: String) {
        mutableWorldActionState.tryEmit(
            WorldActionState.RaceMode(trackUid)
        )
    }

    /**
     * Exit race mode.
     */
    fun exitRaceMode() {
        mutableWorldActionState.tryEmit(
            WorldActionState.StandardMode
        )
    }

    /**
     * Place the world map view in record track mode.
     */
    fun enterRecordingTrackMode() {
        mutableWorldActionState.tryEmit(
            WorldActionState.RecordTrackMode
        )
    }

    /**
     * Exit the track recording mode.
     */
    fun exitRecordingTrackMode() {
        mutableWorldActionState.tryEmit(
            WorldActionState.StandardMode
        )
    }
}