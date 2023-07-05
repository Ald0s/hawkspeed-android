package com.vljx.hawkspeed.ui.screens.authenticated.world.standard

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
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
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.ui.screens.dialogs.trackpreview.TrackPreviewModalBottomSheetScreen
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldObjectsUiState
import com.vljx.hawkspeed.ui.screens.common.DrawCurrentPlayer
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrack
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme

@Composable
fun WorldMapStandardMode(
    standardMode: WorldMapUiState.WorldMapLoadedStandardMode,
    worldObjectsUi: WorldObjectsUiState,
    currentLocation: PlayerPosition?,

    onViewCurrentProfileClicked: ((String) -> Unit)? = null,
    onRaceModeClicked: ((Track) -> Unit)? = null,
    onCreateTrackClicked: (() -> Unit)? = null,
    onViewUserDetail: ((User) -> Unit)? = null,
    onViewTrackDetail: ((Track) -> Unit)? = null,
    onBoundingBoxChanged: ((VisibleRegion, Float) -> Unit)? = null,
    onTrackMarkerClicked: ((Marker, Track) -> Unit)? = null,
    onMapClicked: ((LatLng) -> Unit)? = null,

    componentActivity: ComponentActivity? = null
) {
    // Remember a state for controlling whether we must follow the Player. By default, this is true.
    var shouldFollowPlayer by remember { mutableStateOf<Boolean>(true) }
    // Remember a state for a boolean, this will indicate whether or not we are currently previewing an object.
    var isPreviewingWorldObject by remember { mutableStateOf<Boolean>(false) }
    // Remember the last non-null location as a mutable state. The changing of which will cause recomposition.
    var lastLocation: PlayerPosition by remember { mutableStateOf<PlayerPosition>(currentLocation ?: standardMode.location) }
    // Remember a mutable state for the track UID of the track we wish to preview. Default null meaning nothing to preview.
    var previewingTrackUid: String? by remember { mutableStateOf(null) }

    Scaffold(
        modifier = Modifier,
        floatingActionButton = {
            // If we are following the player, show the floating action button for the menu.
            if(shouldFollowPlayer) {
                FloatingActionButton(
                    onClick = {
                        /**
                         * TODO: show the menu.
                         */
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
                    LatLng(standardMode.location.latitude, standardMode.location.longitude),
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
                if(currentLocation != null) {
                    // Draw the current Player to the map.
                    DrawCurrentPlayer(
                        newPlayerPosition = currentLocation,
                        oldPlayerPosition = lastLocation,
                        isFollowing = shouldFollowPlayer
                    )
                    lastLocation = currentLocation
                }
                // Setup a launched effect that will restart on change of either following player or currently previewing world object. The point of this is to
                // animate the camera to follow the Player, and also to set the tilt for the camera to 0 if player is no longer being followed.
                LaunchedEffect(key1 = shouldFollowPlayer, key2 = isPreviewingWorldObject, block = {
                    // If not previewing world object...
                    if(!isPreviewingWorldObject) {
                        // If should follow player, animate to following them.
                        if(shouldFollowPlayer) {
                            cameraPositionState.animate(
                                lastLocation.toFollowCameraUpdate(
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
                        lastLocation.toFollowCameraUpdate(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowPreviewTrack(
    trackUid: String,
    latestTracksWithPaths: List<TrackWithPath>,
    cameraPositionState: CameraPositionState,
    onViewTrackDetailClicked: ((Track) -> Unit)? = null,
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
                gameSettings = GameSettings(true, "", ""),
                location = PlayerPosition(0.0, 0.0, 0.0f, 0.0f, 0),
                approximateOnly = false
            ),
            worldObjectsUi = WorldObjectsUiState.GotWorldObjects(
                tracks
            ),
            currentLocation = null
        )
    }
}