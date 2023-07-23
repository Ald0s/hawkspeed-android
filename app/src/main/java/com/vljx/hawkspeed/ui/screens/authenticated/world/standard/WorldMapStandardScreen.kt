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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.Extension.noTilt
import com.vljx.hawkspeed.Extension.toFollowCameraUpdate
import com.vljx.hawkspeed.Extension.toOverviewCameraUpdate
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.world.DeviceOrientation
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation
import com.vljx.hawkspeed.ui.screens.dialogs.trackpreview.TrackPreviewModalBottomSheetScreen
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldObjectsUiState
import com.vljx.hawkspeed.ui.screens.common.DrawCurrentPlayer
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrack
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun WorldMapStandardMode(
    standardMode: WorldMapUiState.WorldMapLoadedStandardMode,
    worldObjectsUi: WorldObjectsUiState,
    currentLocationWithOrientation: PlayerPositionWithOrientation?,

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
    onTrackMarkerClicked: ((Marker, Track) -> Unit)? = null,
    onMapClicked: ((LatLng) -> Unit)? = null,

    componentActivity: ComponentActivity? = null
) {
    // Remember a drawer state.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    // Remember a coroutine scope.
    val scope = rememberCoroutineScope()
    // Remember a state for controlling whether we must follow the Player. By default, this is true.
    var shouldFollowPlayer by remember { mutableStateOf<Boolean>(true) }
    // Remember a state for a boolean, this will indicate whether or not we are currently previewing an object.
    var isPreviewingWorldObject by remember { mutableStateOf<Boolean>(false) }
    // Remember the last non-null location as a mutable state. The changing of which will cause recomposition.
    var lastLocationWithOrientation: PlayerPositionWithOrientation by remember {
        mutableStateOf<PlayerPositionWithOrientation>(currentLocationWithOrientation ?: standardMode.locationWithOrientation)
    }
    // Remember a mutable state for the track UID of the track we wish to preview. Default null meaning nothing to preview.
    var previewingTrackUid: String? by remember { mutableStateOf(null) }

    // Setup a modal navigation drawer to surround the google map scaffold.
    ModalNavigationDrawer(
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
                val uiSettings by remember {
                    mutableStateOf(MapUiSettings(
                        compassEnabled = false,
                        myLocationButtonEnabled = false,
                        indoorLevelPickerEnabled = false,
                        mapToolbarEnabled = false,
                        zoomControlsEnabled = false
                    ))
                }
                val mapProperties by remember {
                    mutableStateOf(MapProperties(
                        isBuildingEnabled = false,
                        isIndoorEnabled = false,
                        isMyLocationEnabled = false,
                        mapStyleOptions = componentActivity?.let { activity ->
                            MapStyleOptions.loadRawResourceStyle(
                                activity,
                                R.raw.worldstyle
                            )
                        }
                    ))
                }
                val cameraPositionState = rememberCameraPositionState {
                    // Center the camera initially over our first location, which is provided by the standard mode state.
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(standardMode.locationWithOrientation.position.latitude, standardMode.locationWithOrientation.position.longitude),
                        15f
                    )
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = uiSettings,
                    onMapClick = { latLng ->
                        onMapClicked?.invoke(latLng)
                    }
                ) {
                    // If camera is moving, determine the reason for the move and if this is a User gesture, set should follow player to false.
                    if(cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
                        shouldFollowPlayer = false
                    }
                    // Now, get the visible region in viewport, and as soon as camera stops moving, submit a bounding box changed event.
                    cameraPositionState.projection?.visibleRegion?.let { visibleRegion ->
                        // If visible region is not null, and the camera is not moving, call bounding box changed.
                        if(!cameraPositionState.isMoving) {
                            onBoundingBoxChanged?.let { it(visibleRegion, cameraPositionState.position.zoom) }
                        }
                    }
                    // Draw the current User to the map, we will use the last location here.
                    if(currentLocationWithOrientation != null) {
                        // Draw the current Player to the map.
                        DrawCurrentPlayer(
                            newPlayerPositionWithOrientation = currentLocationWithOrientation,
                            oldPlayerPositionWithOrientation = lastLocationWithOrientation,
                            isFollowing = shouldFollowPlayer
                        )
                        lastLocationWithOrientation = currentLocationWithOrientation
                    }
                    // Setup a launched effect that will restart on change of either following player or currently previewing world object. The point of this is to
                    // animate the camera to follow the Player, and also to set the tilt for the camera to 0 if player is no longer being followed.
                    LaunchedEffect(key1 = shouldFollowPlayer, key2 = isPreviewingWorldObject, block = {
                        // If not previewing world object...
                        if(!isPreviewingWorldObject) {
                            // If should follow player, animate to following them.
                            if(shouldFollowPlayer) {
                                cameraPositionState.animate(
                                    lastLocationWithOrientation.toFollowCameraUpdate(
                                        zoom = 18f
                                    ),
                                    500
                                )
                            } else {
                                // Otherwise, adjust camera to no longer be following; essentially camera is positioned exactly where it is, but with no tilt.
                                cameraPositionState.move(cameraPositionState.position.noTilt())
                            }
                        }
                    })

                    // Now, move the camera to follow the Player, but only if we're currently not previewing anything.
                    if(shouldFollowPlayer && !isPreviewingWorldObject) {
                        cameraPositionState.move(
                            lastLocationWithOrientation.toFollowCameraUpdate(
                                zoom = 18f
                            )
                        )
                    }
                    // Now, draw world objects to the map based on world objects state.
                    when(worldObjectsUi) {
                        is WorldObjectsUiState.GotWorldObjects -> {
                            // Draw world objects to the map. Start with tracks.
                            worldObjectsUi.tracks.forEach { trackWithPath ->
                                DrawRaceTrack(
                                    track = trackWithPath.track,
                                    trackPath = trackWithPath.path,
                                    onTrackMarkerClicked = { marker, track ->
                                        // When the track marker is clicked, set the previewing track UID to the desired track, to show the dialog.
                                        previewingTrackUid = track.trackUid
                                        // Set us as previewing something.
                                        isPreviewingWorldObject = true
                                        // Invoke the on track clicked callback to perform the download of the track's path.
                                        onTrackMarkerClicked?.invoke(marker, track)
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
                        is WorldObjectsUiState.Loading -> {
                            // TODO: loading world objects to map.
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
        Timber.d("Can create tracks: ${account.canCreateTracks}")
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
    HawkSpeedTheme {
        WorldMapStandardMode(
            standardMode = WorldMapUiState.WorldMapLoadedStandardMode(
                playerUid = "USER01",
                account = ExampleData.getExampleAccount(),
                gameSettings = GameSettings(true, "", ""),
                locationWithOrientation = PlayerPositionWithOrientation(
                    PlayerPosition(0.0, 0.0, 0.0f, 0.0f, 0),
                    DeviceOrientation(FloatArray(3))
                ),
                approximateOnly = false
            ),
            worldObjectsUi = WorldObjectsUiState.GotWorldObjects(
                tracks
            ),
            currentLocationWithOrientation = null
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