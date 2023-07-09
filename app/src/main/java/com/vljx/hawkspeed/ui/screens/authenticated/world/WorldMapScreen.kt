package com.vljx.hawkspeed.ui.screens.authenticated.world

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.WorldService
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.ui.MainConfigurePermissions
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState.NonStandardModeFailure.Companion.MISSING_PRECISE_LOCATION_PERMISSION
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceMode
import com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack.WorldMapRecordTrackMode
import com.vljx.hawkspeed.ui.screens.authenticated.world.standard.WorldMapStandardMode
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.util.Extension.getActivity
import timber.log.Timber

@Composable
fun WorldMapScreen(
    onViewCurrentProfileClicked: (String) -> Unit,
    onViewUserDetail: (User) -> Unit,
    onViewTrackDetail: (Track) -> Unit,
    onSetupTrackDetails: (TrackDraftWithPoints) -> Unit,

    worldMapViewModel: WorldMapViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    // Create a new callback here to handle the outcomes of permissions.
    val permissionSettingsCallback: PermissionSettingsCallback = object: PermissionSettingsCallback {
        override fun locationPermissionsUpdated(
            coarseAccessGranted: Boolean,
            fineAccessGranted: Boolean
        ) = worldMapViewModel.updateLocationPermission(coarseAccessGranted, fineAccessGranted)

        override fun locationSettingsAppropriate(appropriate: Boolean) =
            worldMapViewModel.updateLocationSettings(appropriate)
    }
    // Get the current activity we're associated with, and check that it is an instance of MainConfigurePermissions.
    // If so, call for location settings resolution.
    val activityContext = LocalContext.current.getActivity()

    val worldMapUiState: WorldMapUiState by worldMapViewModel.worldMapUiState.collectAsState()
    when(worldMapUiState) {
        is WorldMapUiState.WorldMapLoadedStandardMode -> {
            // Also recompose when current location changes.
            val currentLocation: PlayerPosition? by worldMapViewModel.currentLocation.collectAsState()
            // When map has been loaded call the world map composable.
            val standardMode = worldMapUiState as WorldMapUiState.WorldMapLoadedStandardMode
            // Collect world objects state here.
            val worldObjectsUi by worldMapViewModel.worldObjectsUiState.collectAsState()

            WorldMapStandardMode(
                standardMode = standardMode,
                worldObjectsUi = worldObjectsUi,
                currentLocation = currentLocation,

                onViewCurrentProfileClicked = onViewCurrentProfileClicked,
                onRaceModeClicked = { track ->
                    // When race mode is clicked for a track, we'll call out to view model to enter race mode.
                    worldMapViewModel.enterRaceMode(track.trackUid)
                },
                onCreateTrackClicked = {
                    // TODO: we need to verify the User is actually allowed to create a new track.
                    // When create track is requested, we'll submit a new world action for record track to the view model.
                    // TODO: should set button to disabled somehow?
                    worldMapViewModel.enterRecordingTrackMode()
                },
                onViewUserDetail = onViewUserDetail,
                onViewTrackDetail = onViewTrackDetail,
                onBoundingBoxChanged = worldMapViewModel::updateViewport,
                onTrackMarkerClicked = { marker, track ->
                    // When a track marker has been clicked, use the world map view model to download the desired track's full path.
                    worldMapViewModel.downloadTrack(track)
                },
                onMapClicked = {

                },

                componentActivity = activityContext
            )
        }
        is WorldMapUiState.WorldMapLoadedRaceMode -> {
            val raceMode = worldMapUiState as WorldMapUiState.WorldMapLoadedRaceMode
            WorldMapRaceMode(
                raceMode = raceMode,
                trackUid = raceMode.trackUid,
                onFinishedRace = { race ->
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
        is WorldMapUiState.NonStandardModeFailure -> {
            /**
             * TODO: this is an error state that will lock the view, gray the screen, and display a dialog that communicates the issue at hand, with a button that will reset the
             * TODO: desired mode to Standard mode.
             */
            when((worldMapUiState as WorldMapUiState.NonStandardModeFailure).reason) {
                MISSING_PRECISE_LOCATION_PERMISSION -> throw NotImplementedError()
                else -> throw NotImplementedError()
            }
        }
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
            val notConnected: WorldMapUiState.NotConnected = worldMapUiState as WorldMapUiState.NotConnected
            // We're not connected to server, but can be. Use the provided location to join the world.
            worldMapViewModel.joinWorld(
                notConnected.gameSettings,
                notConnected.location
            )
        }
        is WorldMapUiState.PermissionOrSettingsFailure -> {
            // Could not load map due to permissions or settings, call that composable.
            val worldMapUi = worldMapUiState as WorldMapUiState.PermissionOrSettingsFailure
            ShowPermissionOrSettingsIssue(
                onShouldRetryPermissionAndSettings = {
                    if(activityContext is MainConfigurePermissions) {
                        // Retry will simply call the resolve function again for a retry of that flow.
                        activityContext.resolveLocationPermission(permissionSettingsCallback)
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
        is WorldMapUiState.ConnectionFailure -> {
            /**
             * TODO: connection has failed for some reason. This could either be the server outright refusing our join, or it could be that we ran into some network
             * TODO: trouble or something.
             */
            throw NotImplementedError("WorldMapUiState.ConnectionFailure having a non-null resource error is not yet handled.")
        }
    }

    // Add a disposable side effect to the current lifecycle.
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            // If event is on pause event, set to false.
            if(event == Lifecycle.Event.ON_PAUSE) {
                // Being paused.
            }
            if(event == Lifecycle.Event.ON_RESUME) {
                // On resume, we'll ensure we still have appropriate permissions, and will attempt to resolve this otherwise.
                if(activityContext is MainConfigurePermissions) {
                    // Call for resolution, setting our callback above.
                    activityContext.resolveLocationPermission(
                        permissionSettingsCallback
                    )
                } else {
                    throw IllegalStateException("Activity WorldMapScreen is attached to is not a MainConfigurePermissions!")
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
fun GameSettingsIssue(
    gameSettings: GameSettings?
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

@Composable
fun ShowPermissionOrSettingsIssue(
    onShouldRetryPermissionAndSettings: () -> Unit,
    permissionState: LocationPermissionState,
    settingsState: LocationSettingsState
) {
    // TODO: when we have failed to set the correct permissions or settings, this form will allow the User the chance to tap the allow button and go through
    // TODO that process flow at a later stage.
    Text(text = "Couldn't load map. Either permissions or settings are invalid.")
    Button(
        onClick = onShouldRetryPermissionAndSettings
    ) {
        Text(text = "Retry")
    }
}