package com.vljx.hawkspeed.ui.screens.authenticated.world

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.VisibleRegion
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.ResourceError.GeneralError.Companion.TYPE_DEVICE
import com.vljx.hawkspeed.domain.exc.socket.NetworkConnectivityException
import com.vljx.hawkspeed.domain.exc.socket.NotConnectedException
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.states.socket.WorldSocketState
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation
import com.vljx.hawkspeed.domain.models.world.WorldObjects
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestJoinWorld
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestLeaveWorld
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestViewportUpdate
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.world.RequestGetWorldObjects
import com.vljx.hawkspeed.domain.usecase.account.GetCachedAccountUseCase
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationAndOrientationUseCase
import com.vljx.hawkspeed.domain.usecase.socket.GetCurrentLocationUseCase
import com.vljx.hawkspeed.domain.usecase.socket.GetNetworkConnectivityUseCase
import com.vljx.hawkspeed.domain.usecase.socket.GetWorldSocketStateUseCase
import com.vljx.hawkspeed.domain.usecase.socket.RequestJoinWorldUseCase
import com.vljx.hawkspeed.domain.usecase.socket.RequestLeaveWorldUseCase
import com.vljx.hawkspeed.domain.usecase.socket.SendViewportUpdateUseCase
import com.vljx.hawkspeed.domain.usecase.track.GetTrackWithPathUseCase
import com.vljx.hawkspeed.domain.usecase.world.GetWorldObjectsUseCase
import com.vljx.hawkspeed.ui.MainCheckSensors
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState.NonStandardModeFailure.Companion.MISSING_PRECISE_LOCATION_PERMISSION
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorldMapViewModel @Inject constructor(
    getCurrentLocationAndOrientationUseCase: GetCurrentLocationAndOrientationUseCase,
    getWorldSocketStateUseCase: GetWorldSocketStateUseCase,
    getNetworkConnectivityUseCase: GetNetworkConnectivityUseCase,

    private val getCachedAccountUseCase: GetCachedAccountUseCase,
    private val getWorldObjectsUseCase: GetWorldObjectsUseCase,
    private val getTrackWithPathUseCase: GetTrackWithPathUseCase,

    private val requestJoinWorldUseCase: RequestJoinWorldUseCase,
    private val requestLeaveWorldUseCase: RequestLeaveWorldUseCase,
    private val sendViewportUpdateUseCase: SendViewportUpdateUseCase,

    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
): ViewModel() {
    /**
     * A data class for containing all states that relate to the current device's aptitude for connecting to and using HawkSpeed.
     */
    data class DeviceAptitude(
        val locationPermissionState: LocationPermissionState,
        val locationSettingsState: LocationSettingsState,
        val sensorState: SensorState,
        val networkConnectivity: Boolean
    )

    /**
     * A data class for containing all world socket related objects and emissions.
     */
    data class WorldSocketData(
        val locationWithOrientation: PlayerPositionWithOrientation?,
        val socketState: WorldSocketState
    )

    /**
     * Get the current account cached in storage. We'll configure this as a state flow.
     */
    private val currentCachedAccount: StateFlow<Account?> =
        getCachedAccountUseCase(Unit)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Get the current location from world socket session.
     */
    private val innerCurrentLocationWithOrientation: StateFlow<PlayerPositionWithOrientation?> =
        getCurrentLocationAndOrientationUseCase(Unit)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Get the current world socket session's state.
     */
    private val currentSocketSessionState: SharedFlow<WorldSocketState> =
        getWorldSocketStateUseCase(Unit)

    /**
     * Will only allow emissions if the old is no longer null; which means this flow will not emit anything except the first
     * non-null location after a null location.
     */
    private val currentLocationWithOrientationDistinctOnNull: StateFlow<PlayerPositionWithOrientation?> =
        innerCurrentLocationWithOrientation.distinctUntilChanged { old, new ->
            old != null
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Collapse both the current location with orientation and the world socket state into a single object.
     */
    private val latestWorldSocketData: StateFlow<WorldSocketData?> =
        combine(
            currentLocationWithOrientationDistinctOnNull,
            currentSocketSessionState
        ) { locationWithOrientation, worldSocketState ->
            WorldSocketData(
                locationWithOrientation,
                worldSocketState
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Get the network connectivity.
     */
    private val networkConnectivity: StateFlow<Boolean> =
        getNetworkConnectivityUseCase(Unit)

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
     * A shared flow for the latest sensors state; replay 1 value.
     */
    private val mutableSensorsState: MutableSharedFlow<SensorState> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Combine the location permission, settings state and sensors state into a single entity.
     */
    private val currentDeviceAptitude: Flow<DeviceAptitude> =
        combine(
            mutableLocationPermissionState
                .distinctUntilChanged(),
            mutableLocationSettingsState
                .distinctUntilChanged(),
            mutableSensorsState
                .distinctUntilChanged(),
            networkConnectivity
        ) { permissionState, settingsState, sensorState, network ->
            DeviceAptitude(
                permissionState,
                settingsState,
                sensorState,
                network
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
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorldObjectsUiState.Loading)

    /**
     * Combine all parameters to determine the current state of the world map display. This is the central flow that will change the entire UI.
     */
    val worldMapUiState: StateFlow<WorldMapUiState> =
        combine(
            currentCachedAccount,
            gameSettings,
            mutableWorldActionState,
            currentDeviceAptitude,
            latestWorldSocketData
        ) { account, settings, worldAction, deviceAptitude, worldSocketData ->
            val permissionState = deviceAptitude.locationPermissionState
            val settingsState = deviceAptitude.locationSettingsState
            val sensorState = deviceAptitude.sensorState
            val network = deviceAptitude.networkConnectivity

            return@combine when {
                /**
                 * If account or world socket data is null, we are in a loading state.
                 */
                account == null || worldSocketData == null ->
                    WorldMapUiState.Loading

                /**
                 * If the current device does not have the required sensors, or there's an issue with them, fail.
                 */
                sensorState is SensorState.MissingSensors ->
                    WorldMapUiState.DeviceSensorsIneptFailure(sensorState)

                /**
                 * If the current device does not have network connectivity, emit a ConnectionFailure state with an appropriate reason.
                 * TODO: here's where we can analyse specifics of the network connectivity state, when we augment the value passed from world socket.
                 * TODO: our specifics should then be passed to network connectivity exception.
                 */
                !network ->
                    WorldMapUiState.ConnectionFailure(
                        ResourceError.GeneralError(TYPE_DEVICE, NetworkConnectivityException())
                    )

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
                worldSocketData.locationWithOrientation == null ->
                    WorldMapUiState.NoLocation(null)

                /**
                 * If socket state is disconnected, check the value of resource error. A null resource error means that socket is simply in its natural disconnect state,
                 * and a connection attempt can be attempted. Otherwise, a non-null resource error indicates there is a specific reason disconnection was triggered.
                 */
                worldSocketData.socketState is WorldSocketState.Disconnected ->
                    when(worldSocketData.socketState.resourceError) {
                        null -> WorldMapUiState.NotConnected(
                            settings,
                            worldSocketData.locationWithOrientation
                        )
                        else -> WorldMapUiState.ConnectionFailure(worldSocketData.socketState.resourceError)
                    }

                /**
                 * Emit the loading state if we are currently connecting to the server.
                 */
                worldSocketData.socketState is WorldSocketState.Connecting ->
                    WorldMapUiState.Connecting(
                        worldSocketData.socketState.resourceError
                    )

                /**
                 * If world action requests standard mode, emit the standard mode UI state with all applicable arguments. Coarse location access is allowed
                 * at this point.
                 */
                worldAction is WorldActionState.StandardMode ->
                    WorldMapUiState.WorldMapLoadedStandardMode(
                        (worldSocketData.socketState as WorldSocketState.Connected).playerUid,
                        account,
                        settings,
                        worldSocketData.locationWithOrientation,
                        permissionState is LocationPermissionState.OnlyCoarseGranted
                    )

                /**
                 * If world action is either record track or race mode, and we only have coarse location access, fail with the error world state communicating that
                 * full access is required to perform this action.
                 */
                (permissionState is LocationPermissionState.OnlyCoarseGranted) && (worldAction is WorldActionState.RecordTrackMode || worldAction is WorldActionState.RaceMode) ->
                    WorldMapUiState.NonStandardModeFailure(
                        worldAction,
                        MISSING_PRECISE_LOCATION_PERMISSION
                    )

                /**
                 * If world action is record track mode, we'll emit the appropriate UI state, since we have all arguments satisfied.
                 */
                worldAction is WorldActionState.RecordTrackMode ->
                    WorldMapUiState.WorldMapLoadedRecordTrackMode(
                        (worldSocketData.socketState as WorldSocketState.Connected).playerUid,
                        account,
                        settings,
                        worldSocketData.locationWithOrientation
                    )

                /**
                 * If world action is race mode, we'll emit the appropriate UI state, since we have all arguments satisfied.
                 */
                worldAction is WorldActionState.RaceMode ->
                    WorldMapUiState.WorldMapLoadedRaceMode(
                        (worldSocketData.socketState as WorldSocketState.Connected).playerUid,
                        account,
                        settings,
                        worldSocketData.locationWithOrientation,
                        worldAction.trackUid
                    )

                /**
                 * The default case, unhandled for now.
                 * TODO: handle this.
                 */
                else -> throw NotImplementedError("Failed to emit proper action for worldMapUiState. A case was not handled.")
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorldMapUiState.Loading)

    /**
     * Publicise the current location state flow.
     */
    val currentLocationWithOrientation: StateFlow<PlayerPositionWithOrientation?> =
        innerCurrentLocationWithOrientation

    /**
     * Ensure the socket client is connected to the server, given the game settings and location. If the server is already connected or connecting,
     * this function will not explicitly cause any reconnection until the next connection cycle.
     */
    fun joinWorld(settings: GameSettings, locationWithOrientation: PlayerPositionWithOrientation) {
        requestJoinWorldUseCase(
            RequestJoinWorld(settings, locationWithOrientation.position)
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
        viewModelScope.launch(ioDispatcher) {
            try {
                sendViewportUpdateUseCase(
                    RequestViewportUpdate(
                        visibleRegion.latLngBounds.southwest.longitude,
                        visibleRegion.latLngBounds.southwest.latitude,
                        visibleRegion.latLngBounds.northeast.longitude,
                        visibleRegion.latLngBounds.northeast.latitude,
                        zoom
                    )
                )
            } catch(nce: NotConnectedException) {
                // Nothing to actually do here, we'll just ignore this.
            }
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
     * Update the status of onboard sensors.
     */
    fun updateOnboardSensors(sensorReportMap: Map<Int, MainCheckSensors.SensorReport>) {
        // Get all sensor reports where they are not available.
        val missingSensorReports: List<MainCheckSensors.SensorReport> = sensorReportMap.values
            .filter { !it.gotSensor }
        // If there are any, this is a missing sensors state.
        if(missingSensorReports.isEmpty()) {
            mutableSensorsState.tryEmit(SensorState.AllSensorsPresent)
        } else {
            mutableSensorsState.tryEmit(SensorState.MissingSensors(missingSensorReports))
        }
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
        viewModelScope.launch(ioDispatcher) {
            getTrackWithPathUseCase(
                RequestGetTrackWithPath(track.trackUid)
            ).flowOn(ioDispatcher).collect()
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