package com.vljx.hawkspeed.ui.screens.authenticated.world

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.WorldService
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.ui.MainConfigurePermissions
import com.vljx.hawkspeed.ui.dialogs.trackpreview.TrackPreviewDialog
import com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack.WorldMapRecordTrackMode
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.Extension.getActivity
import timber.log.Timber

@Composable
fun WorldMapScreen(
    onViewCurrentProfileClicked: (String) -> Unit,
    onViewUserDetail: (User) -> Unit,
    onViewTrackDetail: (Track) -> Unit,
    onViewTrackComments: (Track, Boolean) -> Unit,
    onViewTrackLeaderboard: (Track) -> Unit,
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
            // When map has been loaded call the world map composable.
            val worldMapLoaded = worldMapUiState as WorldMapUiState.WorldMapLoadedStandardMode
            // Collect world objects state here.
            val worldObjectsUi by worldMapViewModel.worldObjectsUiState.collectAsState()

            WorldMapStandardMode(
                onViewCurrentProfileClicked = onViewCurrentProfileClicked,
                onCreateTrackClicked = {
                    // TODO: we need to verify the User is actually allowed to create a new track.
                    // When create track is requested, we'll submit a new world action for record track to the view model.
                    // TODO: should set button to disabled somehow?
                    worldMapViewModel.startRecordTrack()
                },
                onViewUserDetail = onViewUserDetail,
                onViewTrackDetail = onViewTrackDetail,
                onViewTrackComments = onViewTrackComments,
                onViewTrackLeaderboard = onViewTrackLeaderboard,

                onBoundingBoxChanged = { boundingBox, zoom ->
                    worldMapViewModel.updateViewport(boundingBox, zoom)
                },
                onTrackMarkerClicked = { marker, track ->
                    // When a track marker has been clicked, use the world map view model to download the desired track's full path.
                    worldMapViewModel.downloadTrack(track)
                },

                standardMode = worldMapLoaded,
                worldObjectsUi = worldObjectsUi
                //worldObjectsUiStateFlow = worldMapViewModel.worldObjectsUiState
            )
        }
        is WorldMapUiState.WorldMapLoadedRaceMode -> {
            /**
             * TODO: world map is engaged in race mode. Call a composable that will set the world up to focus in on the player's current location, lock the camera
             * TODO: to follow the User, and show related overlay UI that allows User to cancel race.
             */
            throw NotImplementedError()
        }
        is WorldMapUiState.WorldMapLoadedRecordTrackMode -> {
            /**
             * TODO: world map is engaged in record track mode. Call a composable that will set the world up to focus in on the player's current location, lock the camera
             * TODO to follow the User, and show related overlay UI that allows User to record, pause, stop, reset the recording - and allows the User to finalise and submit
             * TODO: the new track.
             */
            val recordTrackMode = worldMapUiState as WorldMapUiState.WorldMapLoadedRecordTrackMode
            WorldMapRecordTrackMode(
                onSetupTrackDetails = onSetupTrackDetails,
                onStopRecordingTrack = { draftTrackId, shouldSaveDraft ->
                    // When we want to stop recording the track, pass this up to the view model.
                    worldMapViewModel.stopRecordingTrack(draftTrackId, shouldSaveDraft)
                },
                recordTrackMode = recordTrackMode
            )
        }
        is WorldMapUiState.NonStandardModeFailure -> {
            /**
             * TODO: this is an error state that will lock the view, gray the screen, and display a dialog that communicates the issue at hand, with a button that will reset the
             * TODO: desired mode to Standard mode.
             */
            throw NotImplementedError()
        }
        is WorldMapUiState.Loading -> {
            // Setup the loading composable.
            Loading()
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
        is WorldMapUiState.NoLocation -> {
            // Permission and settings are both OK, but we have received location failure because current location (state flow) has emitted null. This
            // potentially means world service requires a (re)start.
            // TODO: should I use a launched effect? If we get spammed with below timber message, yes.
            Timber.d("Requesting (RE)START of World Service via start command.")
            // Build an intent for world service using above activity context, then start service foreground.
            val worldServiceIntent = Intent(activityContext, WorldService::class.java)
            activityContext.startForegroundService(worldServiceIntent)
            // Setup the loading composable.
            Loading(R.string.world_map_loading_location)
        }
        is WorldMapUiState.NotConnected -> {
            // When we're not connected, this can be an indication or an error; differentiated by whether the resource error is null or not.
            val notConnected: WorldMapUiState.NotConnected = worldMapUiState as WorldMapUiState.NotConnected
            // We're not connected to server, but can be. Use the provided location to join the world.
            worldMapViewModel.joinWorld(
                notConnected.gameSettings,
                notConnected.location
            )
            // Setup the loading composable.
            Loading(R.string.world_map_loading_location)
        }
        is WorldMapUiState.ConnectionFailure -> {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapStandardMode(
    onViewCurrentProfileClicked: (String) -> Unit,
    onCreateTrackClicked: () -> Unit,
    onViewUserDetail: (User) -> Unit,
    onViewTrackDetail: (Track) -> Unit,
    onViewTrackComments: (Track, Boolean) -> Unit,
    onViewTrackLeaderboard: (Track) -> Unit,

    onBoundingBoxChanged: (VisibleRegion, Float) -> Unit,
    onTrackMarkerClicked: (Marker, Track) -> Unit,

    standardMode: WorldMapUiState.WorldMapLoadedStandardMode,
    worldObjectsUi: WorldObjectsUiState
    //worldObjectsUiStateFlow: StateFlow<WorldObjectsUiState>
) {
    var previewingTrackUid: String? by remember { mutableStateOf(null) }

    //val worldObjectsUi by worldObjectsUiStateFlow.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        // Center the camera initially over our first location.
        position = CameraPosition.fromLatLngZoom(
            LatLng(standardMode.location.latitude, standardMode.location.longitude),
            15f
        )
    }

    cameraPositionState.projection?.visibleRegion?.let { visibleRegion ->
        // If visible region is not null, and the camera is not moving, call bounding box changed.
        if(!cameraPositionState.isMoving) {
            onBoundingBoxChanged(visibleRegion, cameraPositionState.position.zoom)
        }
    }

    if(previewingTrackUid != null) {
        TrackPreviewDialog(
            previewingTrackUid!!,
            onViewTrackDetailClicked = onViewTrackDetail,
            onViewTrackLeaderboardClicked = onViewTrackLeaderboard,
            onViewTrackCommentsClicked = onViewTrackComments,
            onDismiss = {
                // When dismiss clicked, we'll set previewing track to null.
                previewingTrackUid = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // TODO: change this back.
                    onCreateTrackClicked()
                    //onViewCurrentProfileClicked(currentUserUid)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "profile"
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false
                ),
                onMapLoaded = {
                    // Map is loaded.
                }
            ) {
                when(worldObjectsUi) {
                    is WorldObjectsUiState.GotWorldObjects -> {
                        val gotWorldObjects = worldObjectsUi as WorldObjectsUiState.GotWorldObjects
                        // Start with tracks.
                        gotWorldObjects.tracks.forEach { trackWithPath ->
                            trackWithPath.track.let { track ->
                                Marker(
                                    state = MarkerState(
                                        position = LatLng(
                                             track.startPoint.latitude,
                                            track.startPoint.longitude
                                        )
                                    ),
                                    title = track.name,
                                    snippet = track.description,
                                    onClick = { trackMarker ->
                                        // TODO: animate and zoom to the track as center.
                                        // When the track marker is clicked, set the previewing track UID to the desired track, to show the dialog.
                                        previewingTrackUid = track.trackUid
                                        // Invoke the on track clicked callback to perform the download of the track's path.
                                        onTrackMarkerClicked(trackMarker, track)
                                        // Return true, since we have handled this ourselves.
                                        return@Marker true
                                    }
                                )
                            }
                            trackWithPath.path?.let { trackPath ->
                                Polyline(
                                    points = trackPath.points.map { trackPoint ->
                                        LatLng(trackPoint.latitude, trackPoint.longitude)
                                    }
                                )
                            }
                        }
                    }
                    is WorldObjectsUiState.Loading -> {
                        // TODO: loading world objects to map.
                    }
                }
            }
        }
    }
}

@Composable
fun Loading(
    @StringRes loadingStringResId: Int = R.string.world_map_loading
) {
    // TODO: have the text here too.
    CircularProgressIndicator()
    Text(text = stringResource(id = loadingStringResId))
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

@Preview(showBackground = true)
@Composable
fun PreviewStandardWorldMap(

) {
    val tracks = remember {
        mutableStateListOf<TrackWithPath>()
    }
    HawkSpeedTheme {
        WorldMapStandardMode(
            onViewCurrentProfileClicked = { },
            onCreateTrackClicked = { },
            onViewUserDetail = { u -> },
            onViewTrackDetail = { t -> },
            onViewTrackComments = { t, b -> },
            onViewTrackLeaderboard = { t -> },

            onBoundingBoxChanged = { vr, f -> },
            onTrackMarkerClicked = { marker, track -> },

            standardMode = WorldMapUiState.WorldMapLoadedStandardMode(
                playerUid = "USER01",
                gameSettings = GameSettings(true, "", ""),
                location = PlayerPosition(0.0, 0.0, 0.0f, 0.0f, 0),
                approximateOnly = false
            ),
            worldObjectsUi = WorldObjectsUiState.GotWorldObjects(
                tracks
            )
        )
    }
}