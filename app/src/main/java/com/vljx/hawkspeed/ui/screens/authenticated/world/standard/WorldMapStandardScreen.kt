package com.vljx.hawkspeed.ui.screens.authenticated.world.standard

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.ui.dialogs.trackpreview.TrackPreviewDialog
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldObjectsUiState
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrack
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ThirdParty
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
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
    onViewTrackComments: ((Track, Boolean) -> Unit)? = null,
    onViewTrackLeaderboard: ((Track) -> Unit)? = null,
    onBoundingBoxChanged: ((VisibleRegion, Float) -> Unit)? = null,
    onTrackMarkerClicked: ((Marker, Track) -> Unit)? = null,
    onMapClicked: ((LatLng) -> Unit)? = null,
    componentActivity: ComponentActivity? = null
) {
    // Remember a state for controlling whether we must follow the Player. By default, this is true.
    var shouldFollowPlayer by remember { mutableStateOf<Boolean>(true) }
    // Remember the last non-null location as a mutable state. The changing of which will cause recomposition.
    var lastLocation: PlayerPosition by remember { mutableStateOf<PlayerPosition>(currentLocation ?: standardMode.location) }
    // Remember a mutable state for the track UID of the track we wish to preview. Default null meaning nothing to preview.
    var previewingTrackUid: String? by remember { mutableStateOf(null) }
    val cameraPositionState = rememberCameraPositionState {
        // Center the camera initially over our first location, which is provided by the standard mode state.
        position = CameraPosition.fromLatLngZoom(
            LatLng(standardMode.location.latitude, standardMode.location.longitude),
            15f
        )
    }
    // Update last location to current location if current location is not null.
    if(currentLocation != null) {
        // TODO: movement animation here for last location to current location.
        lastLocation = currentLocation
    }
    Scaffold(
        modifier = Modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // TODO: change this back.
                    onCreateTrackClicked?.invoke()
                    //onViewCurrentProfileClicked(currentUserUid)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "menu"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapStyleOptions = componentActivity?.let { activity ->
                        MapStyleOptions.loadRawResourceStyle(
                            activity,
                            R.raw.worldstyle
                        )
                    }
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false
                ),
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
                // Now, move the camera to follow the Player, but only if should follow player is true, and previewing track UID is null.
                if(shouldFollowPlayer && previewingTrackUid == null) {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastLocation.latitude, lastLocation.longitude),
                            15f
                        )
                    )
                } else if(previewingTrackUid != null) {
                    TrackPreviewDialog(
                        previewingTrackUid!!,
                        onRaceModeClicked = { onRaceModeClicked?.invoke(it) },
                        onViewTrackDetailClicked = { onViewTrackDetail?.invoke(it) },
                        onViewTrackLeaderboardClicked = { onViewTrackLeaderboard?.invoke(it) },
                        onViewTrackCommentsClicked = { track, wantsToComment -> onViewTrackComments?.invoke(track, wantsToComment) },
                        onDismiss = {
                            // When dismiss clicked, we'll set previewing track to null.
                            previewingTrackUid = null
                        }
                    )
                }
                // Draw the current User to the map, we will use the last location here.
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            lastLocation.latitude,
                            lastLocation.longitude
                        )
                    ),
                    rotation = lastLocation.rotation,
                    icon = ThirdParty.vectorToBitmap(LocalContext.current, R.drawable.ic_car_side, MaterialTheme.colorScheme.primary)
                )
                when(worldObjectsUi) {
                    is WorldObjectsUiState.GotWorldObjects -> {
                        // Draw world objects to the map.
                        val gotWorldObjects = worldObjectsUi as WorldObjectsUiState.GotWorldObjects
                        // Start with tracks.
                        gotWorldObjects.tracks.forEach { trackWithPath ->
                            // TODO: review and centralise how we draw tracks.
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
                                    icon = ThirdParty.vectorToBitmap(LocalContext.current, R.drawable.ic_route, MaterialTheme.colorScheme.primary),
                                    onClick = { trackMarker ->
                                        // When the track marker is clicked, set the previewing track UID to the desired track, to show the dialog.
                                        previewingTrackUid = track.trackUid
                                        // Invoke the on track clicked callback to perform the download of the track's path.
                                        onTrackMarkerClicked?.let { it(trackMarker, track) }
                                        // Return true, since we have handled this ourselves.
                                        return@Marker true
                                    }
                                )
                            }
                            trackWithPath.path?.let { trackPath ->
                                // Create the Track's path.
                                DrawRaceTrack(
                                    points = trackPath.points.map { trackPoint ->
                                        LatLng(trackPoint.latitude, trackPoint.longitude)
                                    }
                                )
                                // Now, check the identity of the Track being viewed, if that is not null and is equal to this track with path, we will animate the camera
                                // to center around the entire track.
                                if(previewingTrackUid == trackWithPath.track.trackUid) {
                                    // Get the path's bounding box.
                                    val trackBoundingBox = trackPath.getBoundingBox()
                                    // Open a new launched effect here that will key off the track UID being previewed.
                                    LaunchedEffect(key1 = previewingTrackUid, block = {
                                        // TODO: delete debug message.
                                        Timber.d("Moving camera to focus on track.")
                                        // Animate the camera
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngBounds(
                                                LatLngBounds(
                                                    LatLng(trackBoundingBox.southWest.latitude, trackBoundingBox.southWest.longitude),
                                                    LatLng(trackBoundingBox.northEast.latitude, trackBoundingBox.northEast.longitude)
                                                ),
                                                25
                                            ),
                                            2500
                                        )
                                    })
                                }
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

@Preview(showBackground = true)
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