package com.vljx.hawkspeed.ui.screens.authenticated.world

import android.content.Intent
import android.hardware.Sensor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.WorldService
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.ResourceError.GeneralError.Companion.TYPE_DEVICE
import com.vljx.hawkspeed.domain.ResourceError.GeneralError.Companion.TYPE_HTTP
import com.vljx.hawkspeed.domain.ResourceError.GeneralError.Companion.TYPE_SOCKET
import com.vljx.hawkspeed.domain.exc.socket.ConnectionBrokenException
import com.vljx.hawkspeed.domain.exc.socket.NetworkConnectivityException
import com.vljx.hawkspeed.domain.exc.socket.ServerUnavailableException
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation
import com.vljx.hawkspeed.ui.MainCheckSensors
import com.vljx.hawkspeed.ui.MainConfigurePermissions
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState.NonStandardModeFailure.Companion.MISSING_PRECISE_LOCATION_PERMISSION
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceMode
import com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack.WorldMapRecordTrackMode
import com.vljx.hawkspeed.ui.screens.authenticated.world.standard.WorldMapStandardMode
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.Extension.getActivity
import timber.log.Timber

@Composable
fun WorldMapScreen(
    onViewCurrentProfileClicked: (String) -> Unit,
    onViewVehiclesClicked: (String) -> Unit,
    onViewTracksClicked: (String) -> Unit,
    onSettingsClicked: () -> Unit,

    onViewUserDetail: (User) -> Unit,
    onViewTrackDetail: (Track) -> Unit,
    onViewRaceLeaderboardDetail: (RaceLeaderboard) -> Unit,
    onSetupTrackDetails: (TrackDraftWithPoints) -> Unit,

    worldMapViewModel: WorldMapViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    // Create a new callback here to handle the outcomes of permissions.
    val locationPermissionSettingsCallback: LocationPermissionSettingsCallback =
        object : LocationPermissionSettingsCallback {
            override fun locationPermissionsUpdated(
                coarseAccessGranted: Boolean,
                fineAccessGranted: Boolean
            ) = worldMapViewModel.updateLocationPermission(coarseAccessGranted, fineAccessGranted)

            override fun locationSettingsAppropriate(appropriate: Boolean) =
                worldMapViewModel.updateLocationSettings(appropriate)
        }
    // Create a new callback here to handle outcome of activity recog permission.
    val activityRecognitionPermissionCallback: ActivityRecognitionPermissionCallback =
        object : ActivityRecognitionPermissionCallback {
            override fun activityRecognitionPermissionGranted() =
                worldMapViewModel.updateActivityRecognitionPermission(true)

            override fun activityRecognitionPermissionRefused() =
                worldMapViewModel.updateActivityRecognitionPermission(false)
        }
    // Get the current activity we're associated with, and check that it is an instance of MainConfigurePermissions.
    // If so, call for location settings resolution.
    val activityContext = LocalContext.current.getActivity()

    val worldMapUiState: WorldMapUiState by worldMapViewModel.worldMapUiState.collectAsStateWithLifecycle()
    when (worldMapUiState) {
        is WorldMapUiState.WorldMapLoadedStandardMode -> {
            // Also recompose when current location changes.
            val currentLocationWithOrientation: PlayerPositionWithOrientation? by worldMapViewModel.currentLocationWithOrientation.collectAsStateWithLifecycle()
            // When map has been loaded call the world map composable.
            val standardMode = worldMapUiState as WorldMapUiState.WorldMapLoadedStandardMode
            // Collect world objects state here.
            val worldObjectsUi by worldMapViewModel.worldObjectsUiState.collectAsStateWithLifecycle()

            WorldMapStandardMode(
                standardMode = standardMode,
                worldObjectsUi = worldObjectsUi,
                locationWithOrientation = currentLocationWithOrientation
                    ?: standardMode.locationWithOrientation,

                onViewCurrentProfileClicked = onViewCurrentProfileClicked,
                onViewVehiclesClicked = onViewVehiclesClicked,
                onViewTracksClicked = onViewTracksClicked,
                onTrackRecorderClicked = {
                    if(!(worldMapUiState as WorldMapUiState.WorldMapLoadedStandardMode).account.canCreateTracks) {
                        // If we are not able to create tracks, just return.
                        return@WorldMapStandardMode
                    }
                    worldMapViewModel.enterRecordingTrackMode()
                },
                onSettingsClicked = onSettingsClicked,

                onRaceModeClicked = { track ->
                    // When race mode is clicked for a track, we'll call out to view model to enter race mode.
                    worldMapViewModel.enterRaceMode(track.trackUid)
                },
                onViewUserDetail = onViewUserDetail,
                onViewTrackDetail = onViewTrackDetail,
                onViewRaceLeaderboardDetail = onViewRaceLeaderboardDetail,

                onBoundingBoxChanged = worldMapViewModel::updateViewport,
                onTrackMarkerClicked = { track ->
                    // When a track marker has been clicked, use the world map view model to download the desired track's full path.
                    worldMapViewModel.downloadTrack(track)
                },

                componentActivity = activityContext
            )
        }

        is WorldMapUiState.WorldMapLoadedRaceMode -> {
            val raceMode = worldMapUiState as WorldMapUiState.WorldMapLoadedRaceMode
            WorldMapRaceMode(
                raceMode = raceMode,
                trackUid = raceMode.trackUid,
                onFinishedRace = { race, raceLeaderboard ->
                    throw NotImplementedError("WorldMapLoadedRaceMode resulted in a successful race! But the handling of this in WorldMapScreen is not done yet.")
                },
                onExitRaceMode = {
                    // When we have returned from race mode, simply set back to standard mode.
                    worldMapViewModel.exitRaceMode()
                }
            )
        }

        is WorldMapUiState.WorldMapLoadedRecordTrackMode -> {
            val recordTrackMode = worldMapUiState as WorldMapUiState.WorldMapLoadedRecordTrackMode
            WorldMapRecordTrackMode(
                recordTrackMode = recordTrackMode,
                onSetupTrackDetails = onSetupTrackDetails,
                onCancelRecordingClicked = {
                    // When we want to stop recording the track, pass this up to the view model.
                    worldMapViewModel.exitRecordingTrackMode()
                }
            )
        }

        is WorldMapUiState.NonStandardModeFailure ->
            HandleNonStandardModeFailure(
                nonStandardModeFailure = worldMapUiState as WorldMapUiState.NonStandardModeFailure
            )

        is WorldMapUiState.Loading -> {
            // Setup the loading composable.
            LoadingScreen()
        }

        is WorldMapUiState.NoLocation -> {
            // Setup the loading composable.
            LoadingScreen(R.string.world_map_loading_location)
            // Permission and settings are both OK, but we have received location failure because current location (state flow) has emitted null. This
            // potentially means world service requires a (re)start.
            // TODO: should I use a launched effect? If we get spammed with below timber message, yes.
            Timber.d("Requesting (RE)START of World Service via start command.")
            // Build an intent for world service using above activity context, then start service foreground.
            val worldServiceIntent = Intent(activityContext, WorldService::class.java)
            activityContext.startForegroundService(worldServiceIntent)
        }

        is WorldMapUiState.NotConnected -> {
            // Setup the loading composable.
            LoadingScreen(R.string.world_map_loading_connecting)
            // When we're not connected, this can be an indication or an error; differentiated by whether the resource error is null or not.
            val notConnected: WorldMapUiState.NotConnected =
                worldMapUiState as WorldMapUiState.NotConnected
            // We're not connected to server, but can be. Use the provided location to join the world.
            worldMapViewModel.joinWorld(
                notConnected.gameSettings,
                notConnected.locationWithOrientation
            )
        }

        is WorldMapUiState.Connecting ->
            WorldMapLoading(worldMapUiState as WorldMapUiState.Connecting)

        is WorldMapUiState.DeviceSensorsIneptFailure ->
            HandleDeviceSensorsIneptFailure(
                deviceSensorsIneptFailure = worldMapUiState as WorldMapUiState.DeviceSensorsIneptFailure
            )

        is WorldMapUiState.PermissionOrSettingsFailure -> {
            // Could not load map due to permissions or settings, call that composable.
            val worldMapUi = worldMapUiState as WorldMapUiState.PermissionOrSettingsFailure
            ShowPermissionOrSettingsIssue(
                onShouldRetryPermissionAndSettings = {
                    if (activityContext is MainConfigurePermissions) {
                        // Retry will simply call the resolve function again for a retry of that flow.
                        activityContext.resolveLocationPermission(locationPermissionSettingsCallback)
                    }
                },
                permissionState = worldMapUi.permissionState,
                settingsState = worldMapUi.settingsState
            )
        }

        is WorldMapUiState.GameSettingsFailure -> {
            // The Player has configured their app to forcefully avoid connecting to game server.
            val gameSettingsFailure = worldMapUiState as WorldMapUiState.GameSettingsFailure
            GameSettingsIssue(
                gameSettings = gameSettingsFailure.gameSettings
            )
        }

        is WorldMapUiState.ConnectionFailure ->
            HandleConnectionFailure(worldMapUiState as WorldMapUiState.ConnectionFailure)
    }

    // Add a disposable side effect to the current lifecycle.
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            // If event is on pause event, set to false.
            if (event == Lifecycle.Event.ON_PAUSE) {
                // Being paused.
            }
            if (event == Lifecycle.Event.ON_RESUME) {
                // On resume, we'll ensure we still have appropriate permissions, and will attempt to resolve this otherwise.
                if (activityContext is MainConfigurePermissions) {
                    // Call for resolution, setting our callback above.
                    activityContext.resolveLocationPermission(
                        locationPermissionSettingsCallback
                    )
                    // Call for resolution for activity recog API.
                    activityContext.resolveActivityRecognitionPermission(
                        activityRecognitionPermissionCallback
                    )
                } else {
                    throw IllegalStateException("Activity WorldMapScreen is attached to is not a MainConfigurePermissions!")
                }
                // Also on resume, get a report of device sensors and their states.
                if (activityContext is MainCheckSensors) {
                    // Call for report.
                    val sensorReportsMap = activityContext.checkSensors(
                        listOf(
                            Sensor.TYPE_ACCELEROMETER,
                            Sensor.TYPE_GYROSCOPE,
                            Sensor.TYPE_MAGNETIC_FIELD,
                            Sensor.TYPE_ROTATION_VECTOR
                        )
                    )
                    // With results, update view model.
                    worldMapViewModel.updateOnboardSensors(sensorReportsMap)
                } else {
                    throw IllegalStateException("Activity WorldMapScreen is attached to is not a MainCheckSensors!")
                }
            }
        }
        // Add the observer to lifecycle.
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            // Remove observer from lifecycle.
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })
}

@Composable
fun HandleNonStandardModeFailure(
    nonStandardModeFailure: WorldMapUiState.NonStandardModeFailure
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            /**
             * TODO: failed to enter whatever non standard mode was requested. This should let the User know what happened and what they must do in order to
             * TODO: correctly use that mode.
             * when((worldMapUiState as WorldMapUiState.NonStandardModeFailure).reason) {
                MISSING_PRECISE_LOCATION_PERMISSION -> throw NotImplementedError()
                else -> throw NotImplementedError()
                }
             */
            throw NotImplementedError()
        }
    }
}

@Composable
fun WorldMapLoading(
    connecting: WorldMapUiState.Connecting
) {
    // Now, based on the reason for connecting, display an appropriate loading screen.
    LoadingScreen(
        when(val resourceError = connecting.resourceError) {
            /**
             * TODO: set up different reasons for the GeneralError, ApiError and SocketError loading outcome states.
             * Reasons here should be an answer to the question; we are currently retrying connection to the game server BECAUSE...
             * Then the appropriate message or interface.
             */
            is ResourceError.GeneralError ->
                when(val exception = resourceError.exception) {
                    is ServerUnavailableException -> R.string.world_map_loading_connecting
                    is ConnectionBrokenException -> R.string.world_map_loading_connecting
                    else -> throw NotImplementedError()
                }
            is ResourceError.ApiError -> R.string.world_map_loading_connecting
            is ResourceError.SocketError -> R.string.world_map_loading_connecting
            else -> R.string.world_map_loading_connecting
        }
    )
}

@Composable
fun HandleDeviceSensorsIneptFailure(
    deviceSensorsIneptFailure: WorldMapUiState.DeviceSensorsIneptFailure
) {
    TODO("Not yet implemented")
}

@Composable
fun HandleConnectionFailure(
    connectionFailure: WorldMapUiState.ConnectionFailure
) {
    /**
     * TODO: this is a connection failure; specifically for the game server. We should place an interface here that explains why connection failed,
     * TODO: and perhaps controls to retry the connection depending on that reason. From this area though, we should be able to still access all
     * TODO: social functions such as profile, tracks list etc.
     */
    when(val resourceError = connectionFailure.resourceError) {
        is ResourceError.GeneralError -> when(resourceError.messageOrType) {
            TYPE_DEVICE -> when(resourceError.exception) {
                is NetworkConnectivityException -> {
                    Timber.d("Connection to game server failed - network connectivity is not available.")
                }
                else -> throw NotImplementedError()
            }
            TYPE_SOCKET -> throw NotImplementedError()
            TYPE_HTTP -> throw NotImplementedError()
        }
        is ResourceError.SocketError -> throw NotImplementedError()
        is ResourceError.ApiError -> throw NotImplementedError()
        else -> throw NotImplementedError()
    }
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            Text(text = "Connection Failure")
        }
    }
}

@Composable
fun GameSettingsIssue(
    gameSettings: GameSettings?
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            if(gameSettings == null) {
                // TODO: no game settings retrieved, maybe none cached.
                throw NotImplementedError("GameSettingsIssue does not handle gameSettings being NULL")
            } else {
                if(!gameSettings.canConnectGame) {
                    Text(text = "You have configured HawkSpeed to avoid connecting to game server.")
                    // TODO: UI.
                } else {
                    // TODO: the server is currently not running.
                    throw NotImplementedError("GameSettingsIssue does not handle canConnectGame evaluating false")
                }
            }
        }
    }
}

@Composable
fun ShowPermissionOrSettingsIssue(
    permissionState: LocationPermissionState,
    settingsState: LocationSettingsState,

    onShouldRetryPermissionAndSettings: (() -> Unit)? = null
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            // TODO: when we have failed to set the correct permissions or settings, this form will allow the User the chance to tap the allow button and go through
            // TODO that process flow at a later stage.
            Text(text = "Couldn't load map. Either permissions or settings are invalid.")
            Button(
                onClick = {
                    onShouldRetryPermissionAndSettings?.invoke()
                }
            ) {
                Text(text = "Retry")
            }
        }
    }
}

@Preview
@Composable
fun PreviewHandleNonStandardModeFailure(

) {
    HawkSpeedTheme {
        HandleNonStandardModeFailure(
            WorldMapUiState.NonStandardModeFailure(
                WorldActionState.RaceMode("TRACK01"),
                MISSING_PRECISE_LOCATION_PERMISSION
            )
        )
    }
}

@Preview
@Composable
fun PreviewWorldMapLoading(

) {
    HawkSpeedTheme {
        WorldMapLoading(
            connecting = WorldMapUiState.Connecting(
                ResourceError.GeneralError(ResourceError.GeneralError.TYPE_SOCKET, ServerUnavailableException())
            )
        )
    }
}

@Preview
@Composable
fun PreviewHandleDeviceSensorsIneptFailure(

) {
    HawkSpeedTheme {
        HandleDeviceSensorsIneptFailure(
            deviceSensorsIneptFailure = WorldMapUiState.DeviceSensorsIneptFailure(
                SensorState.MissingSensors(listOf(MainCheckSensors.SensorReport(Sensor.TYPE_ACCELEROMETER, false)))
            )
        )
    }
}

@Preview
@Composable
fun PreviewHandleConnectionFailure(

) {
    HawkSpeedTheme {
        HandleConnectionFailure(
            connectionFailure = WorldMapUiState.ConnectionFailure(
                ResourceError.GeneralError(ResourceError.GeneralError.TYPE_SOCKET, ServerUnavailableException())
            )
        )
    }
}

@Preview
@Composable
fun PreviewGameSettingsIssue(

) {
    HawkSpeedTheme {
        GameSettingsIssue(
            gameSettings = GameSettings(true, null, null)
        )
    }
}

@Preview
@Composable
fun PreviewShowPermissionOrSettingsIssue(

) {
    HawkSpeedTheme {
        ShowPermissionOrSettingsIssue(
            onShouldRetryPermissionAndSettings = { /*TODO*/ },
            permissionState = LocationPermissionState.AllGranted,
            settingsState = LocationSettingsState.Appropriate
        )
    }
}