package com.vljx.hawkspeed.ui.screens.authenticated.world.standard

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.Extension.TILT_DEFAULT
import com.vljx.hawkspeed.Extension.noTilt
import com.vljx.hawkspeed.Extension.overlapsWith
import com.vljx.hawkspeed.Extension.toBoundingBox
import com.vljx.hawkspeed.Extension.toFollowCameraPosition
import com.vljx.hawkspeed.Extension.toOverviewCameraUpdate
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.world.CurrentPlayer
import com.vljx.hawkspeed.domain.models.world.DeviceOrientation
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation
import com.vljx.hawkspeed.ui.component.mapoverlay.DrawCurrentPlayer
import com.vljx.hawkspeed.ui.component.mapoverlay.MapOverlay
import com.vljx.hawkspeed.ui.screens.dialogs.trackpreview.TrackPreviewModalBottomSheetScreen
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldObjectsUiState
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrack
import com.vljx.hawkspeed.ui.screens.common.RaceTrackDisplayMode
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * TODO: things still to fix/work on with respect to map overlays.
 * 1. Currently, we rotate the (already rotated) player overlay to ensure rotating the camera does not interfere with the bearing of that player; but we should instead mediate between the player's
 * rotation and the camera's rotation to find the correct angle such that irrespective of whether the player rotates or the camera rotates, we're viewing the correct angle of the player.
 * 2. We must draw tracks & track paths via overlay as well, since we want the enhanced abilities
 */
@Composable
fun WorldMapStandardMode(
    standardMode: WorldMapUiState.WorldMapLoadedStandardMode,
    worldObjectsUi: WorldObjectsUiState,
    locationWithOrientation: PlayerPositionWithOrientation,

    onViewCurrentProfileClicked: ((String) -> Unit)? = null,
    onViewVehiclesClicked: ((String) -> Unit)? = null,
    onViewTracksClicked: ((String) -> Unit)? = null,
    onTrackRecorderClicked: (() -> Unit)? = null,
    onSettingsClicked: (() -> Unit)? = null,

    onRaceModeClicked: ((Track) -> Unit)? = null,
    onViewUserDetail: ((User) -> Unit)? = null,
    onViewTrackDetail: ((Track) -> Unit)? = null,
    onViewRaceLeaderboardDetail: ((RaceLeaderboard) -> Unit)? = null,

    onBoundingBoxChanged: ((VisibleRegion, Float) -> Unit)? = null,
    onTrackMarkerClicked: ((Track) -> Unit)? = null,
    onMapClicked: ((LatLng) -> Unit)? = null,

    componentActivity: ComponentActivity? = null
) {
    // Remember a drawer state.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    // Remember a coroutine scope.
    val scope = rememberCoroutineScope()
    // Remember a state for controlling whether we must follow the Player. By default, this is true.
    var shouldFollowPlayer by remember { mutableStateOf<Boolean>(true) }
    // If follow mode enabled, this camera position will be moved to.
    var followCameraPosition by remember {
        mutableStateOf<CameraPosition?>(null)
    }
    // Remember a state for a boolean, this will indicate whether or not we are currently previewing an object.
    var isPreviewingWorldObject by remember { mutableStateOf<Boolean>(false) }
    // Remember a mutable state for the track UID of the track we wish to preview. Default null meaning nothing to preview.
    var previewingTrackUid: String? by remember { mutableStateOf(null) }

    val uiSettings by remember {
        mutableStateOf(MapUiSettings(
            compassEnabled = false,
            myLocationButtonEnabled = false,
            indoorLevelPickerEnabled = false,
            mapToolbarEnabled = false,
            zoomControlsEnabled = false,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = false
        ))
    }

    val mapProperties by remember {
        mutableStateOf(MapProperties(
            isBuildingEnabled = false,
            isIndoorEnabled = false,
            isMyLocationEnabled = false,
            isTrafficEnabled = false,
            mapStyleOptions = componentActivity?.let { activity ->
                MapStyleOptions.loadRawResourceStyle(
                    activity,
                    R.raw.worldstyle
                )
            }
        ))
    }

    val cameraPositionState = rememberCameraPositionState {
        // Center the camera depending on whether we should be following the player or not.
        position = when(shouldFollowPlayer) {
            true -> locationWithOrientation.toFollowCameraPosition()
            else ->
                CameraPosition.builder()
                    .target(LatLng(standardMode.locationWithOrientation.position.latitude, standardMode.locationWithOrientation.position.longitude))
                    .zoom(15f)
                    .tilt(TILT_DEFAULT)
                    .build()
        }
    }

    // Setup a modal navigation drawer to surround the google map scaffold.
    ModalNavigationDrawer(
        gesturesEnabled = drawerState.isOpen,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    account = standardMode.account,

                    onProfileClicked = { onViewCurrentProfileClicked?.invoke(standardMode.playerUid) },
                    onViewVehiclesClicked = { onViewVehiclesClicked?.invoke(standardMode.playerUid) },
                    onViewTracksClicked = { onViewTracksClicked?.invoke(standardMode.playerUid) },
                    onTrackRecorderClicked = { onTrackRecorderClicked?.invoke() },
                    onSettingsClicked = { onSettingsClicked?.invoke() }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier,
            floatingActionButton = {
                // If we are following the player, show the floating action button for the menu.
                if(shouldFollowPlayer) {
                    FloatingActionButton(
                        onClick = {
                            // When the menu button is clicked, toggle drawer open.
                            scope.launch {
                                if(drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "menu"
                        )
                    }
                } else {
                    // If we are not following the Player, show the 'My Location' Button, which will take the camera back to following the Player.
                    FloatingActionButton(
                        onClick = {
                            shouldFollowPlayer = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "location"
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
            ) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = uiSettings,
                    onMapClick = { latLng ->
                        onMapClicked?.invoke(latLng)
                    }
                ) {
                    LaunchedEffect(
                        key1 = cameraPositionState.isMoving,
                        key2 = cameraPositionState.position.bearing,
                        block = {
                            if(cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
                                shouldFollowPlayer = false

                                if(!cameraPositionState.isMoving) {
                                    cameraPositionState.projection?.visibleRegion?.let { visibleRegion ->
                                        onBoundingBoxChanged?.invoke(visibleRegion, cameraPositionState.position.zoom)
                                    }
                                }
                            }
                        }
                    )

                    LaunchedEffect(
                        key1 = shouldFollowPlayer,
                        key2 = isPreviewingWorldObject,
                        block = {
                            // If not previewing world object...
                            if(!isPreviewingWorldObject) {
                                // If should follow player, animate to following them.
                                if(shouldFollowPlayer) {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newCameraPosition(
                                            locationWithOrientation.toFollowCameraPosition()
                                        ),
                                        500
                                    )
                                }
                            }
                        }
                    )

                    LaunchedEffect(key1 = followCameraPosition, block = {
                        if(shouldFollowPlayer && followCameraPosition != null && !cameraPositionState.isMoving) {
                            cameraPositionState.move(
                                CameraUpdateFactory.newCameraPosition(
                                    followCameraPosition!!
                                )
                            )
                        }
                    })

                    if(worldObjectsUi is WorldObjectsUiState.CurrentWorldObjects) {
                        cameraPositionState.projection?.visibleRegion?.let { currentVisibleRegion ->
                            // Determine latest display mode for tracks.
                            val trackDisplayMode = when {
                                cameraPositionState.position.zoom < 15f && cameraPositionState.position.zoom >= 12f ->
                                    RaceTrackDisplayMode.Partial
                                cameraPositionState.position.zoom < 12f ->
                                    RaceTrackDisplayMode.None
                                else -> RaceTrackDisplayMode.Full
                            }
                            worldObjectsUi.tracks.forEach { trackWithPath ->
                                if(currentVisibleRegion.latLngBounds.contains(LatLng(trackWithPath.track.startPoint.latitude, trackWithPath.track.startPoint.longitude))
                                    || (trackWithPath.path?.getBoundingBox()?.let { currentVisibleRegion.toBoundingBox().overlapsWith(it) } == true)) {
                                    DrawRaceTrack(
                                        track = trackWithPath.track,
                                        trackPath = trackWithPath.path,
                                        displayMode = trackDisplayMode,
                                        onTrackMarkerClicked = { track ->
                                            // When the track marker is clicked, set the previewing track UID to the desired track, to show the dialog.
                                            previewingTrackUid = track.trackUid
                                            // Set us as previewing something.
                                            isPreviewingWorldObject = true
                                            // Invoke the on track clicked callback to perform the download of the track's path.
                                            onTrackMarkerClicked?.invoke(track)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Here, we'll draw the overlay.
                if(worldObjectsUi is WorldObjectsUiState.CurrentWorldObjects) {
                    MapOverlay(
                        cameraPositionState = cameraPositionState
                    ) {
                        DrawCurrentPlayer(
                            currentPlayer = worldObjectsUi.currentPlayer,
                            isBeingFollowed = shouldFollowPlayer,
                            onNewCameraPosition = { latLng, rotation ->
                                followCameraPosition = PlayerPosition(latLng.latitude, latLng.longitude, rotation, 0f, 0L)
                                    .toFollowCameraPosition()
                            }
                        )
                    }

                    // After drawing everything to the map, check whether we are previewing anything,
                    if(isPreviewingWorldObject) {
                        // Now, decide which what we're previewing and call the appropriate composable.
                        if(previewingTrackUid != null) {
                            // Otherwise, if we have a previewing track Uid, call the show track preview composable to handle everything else.
                            ShowPreviewTrack(
                                previewingTrackUid!!,
                                worldObjectsUi.tracks,
                                cameraPositionState,
                                onRaceModeClicked = onRaceModeClicked,
                                onViewTrackDetailClicked = onViewTrackDetail,
                                onViewUserDetail = onViewUserDetail,
                                onViewRaceLeaderboardDetail = onViewRaceLeaderboardDetail,
                                onDismiss = {
                                    // Set the track being previewed to null.
                                    previewingTrackUid = null
                                    // Set us as no longer previewing anything.
                                    isPreviewingWorldObject = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    account: Account,

    onProfileClicked: (() -> Unit)? = null,
    onViewVehiclesClicked: (() -> Unit)? = null,
    onViewTracksClicked: (() -> Unit)? = null,
    onTrackRecorderClicked: (() -> Unit)? = null,
    onSettingsClicked: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(16.dp),
            letterSpacing = 2.8.sp,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            text = stringResource(id = R.string.app_name).uppercase()
        )
        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.profile)) },
            icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "profile") },
            selected = false,
            onClick = onProfileClicked ?: {}
        )
        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.vehicles)) },
            icon = {
                Image(
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                    painter = painterResource(id = R.drawable.ic_car_side),
                    contentDescription = "vehicles"
                )
            },
            selected = false,
            onClick = onViewVehiclesClicked ?: {}
        )
        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.tracks)) },
            icon = {
                Image(
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                    painter = painterResource(id = R.drawable.ic_route),
                    contentDescription = "tracks"
                )
            },
            selected = false,
            onClick = onViewTracksClicked ?: {}
        )
        Divider(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
        )
        Text(
            modifier = Modifier
                .padding(16.dp),
            text = stringResource(R.string.subtitle_create)
        )
        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.track_recorder)) },
            icon = {
                Image(
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                    painter = painterResource(id = R.drawable.video),
                    contentDescription = "track recorder"
                )
            },
            selected = false,
            onClick = onTrackRecorderClicked ?: {}
        )
        Divider(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )
        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.settings)) },
            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "settings") },
            selected = false,
            onClick = onSettingsClicked ?: {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowPreviewTrack(
    trackUid: String,
    latestTracksWithPaths: List<TrackWithPath>,
    cameraPositionState: CameraPositionState,
    onViewTrackDetailClicked: ((Track) -> Unit)? = null,
    onViewUserDetail: ((User) -> Unit)? = null,
    onViewRaceLeaderboardDetail: ((RaceLeaderboard) -> Unit)? = null,
    onRaceModeClicked: ((Track) -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    // Remember a state for a modal bottom sheet state, for objects we wish to preview.
    val previewModalBottomSheetState = rememberModalBottomSheetState()
    // Remember our track with path.
    var trackWithPath: TrackWithPath? by remember {
        mutableStateOf(null)
    }
    // Iterate all latest tracks with their paths, searching for the previewed one. If we find it, set the track with path.
    latestTracksWithPaths.find { it.track.trackUid == trackUid }?.let { targetTrackWithPath ->
        // If Path is not null, perform an animation to view the whole track.
        if(targetTrackWithPath.path != null) {
            LaunchedEffect(key1 = trackUid, block = {
                val trackPath = targetTrackWithPath.path!!
                // Get the path's bounding box.
                val trackBoundingBox = trackPath.getBoundingBox()
                // Animate the camera to view a track overview.
                cameraPositionState.animate(trackBoundingBox.toOverviewCameraUpdate(), 500)
                // After animating, set the previewed track.
                trackWithPath = targetTrackWithPath
            })
        }
    }
    // On a recomposition, if track with path is not null, which means animation is complete, we'll show the preview.
    if(trackWithPath != null) {
        TrackPreviewModalBottomSheetScreen(
            track = trackWithPath!!.track,
            onRaceModeClicked = onRaceModeClicked,
            onViewTrackDetailClicked = onViewTrackDetailClicked,
            onViewUserDetail = onViewUserDetail,
            onViewRaceLeaderboardDetail = onViewRaceLeaderboardDetail,
            onDismiss = {
                trackWithPath = null
                onDismiss?.invoke()
            },
            sheetState = previewModalBottomSheetState
        )
    }
}

@Preview
@Composable
fun PreviewStandardWorldMap(

) {
    val tracks = remember {
        mutableStateListOf<TrackWithPath>()
    }
    val players = remember {
        mutableStateListOf<PlayerPosition>()
    }
    HawkSpeedTheme {
        val standardMode = WorldMapUiState.WorldMapLoadedStandardMode(
            playerUid = "USER01",
            account = ExampleData.getExampleAccount(),
            gameSettings = GameSettings(true, "", ""),
            locationWithOrientation = PlayerPositionWithOrientation(
                PlayerPosition(0.0, 0.0, 0.0f, 0.0f, 0),
                DeviceOrientation(FloatArray(3))
            ),
            approximateOnly = false
        )

        WorldMapStandardMode(
            standardMode = standardMode,
            worldObjectsUi = WorldObjectsUiState.CurrentWorldObjects(
                CurrentPlayer(
                    ExampleData.getExampleAccount(),
                    GameSettings(true, "", ""),
                    PlayerPosition(0.0, 0.0, 0.0f, 0.0f, 0)
                ),
                tracks
            ),
            locationWithOrientation = standardMode.locationWithOrientation
        )
    }
}

@Preview
@Composable
fun PreviewDrawerContent(

) {
    HawkSpeedTheme {
        Surface {
            Column {
                DrawerContent(
                    account = ExampleData.getExampleAccount()
                )
            }
        }
    }
}