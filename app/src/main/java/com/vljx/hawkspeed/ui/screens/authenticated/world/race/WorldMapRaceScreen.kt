package com.vljx.hawkspeed.ui.screens.authenticated.world.race

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState
import com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack.WorldMapRecordTrackUiState
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrack
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.Extension.getActivity
import timber.log.Timber

/**
 * TODO: world map is engaged in race mode. Call a composable that will set the world up to focus in on the player's current location, lock the camera
 * TODO: to follow the User, and show related overlay UI that allows User to cancel race.
 */
@Composable
fun WorldMapRaceMode(
    raceMode: WorldMapUiState.WorldMapLoadedRaceMode,
    trackUid: String,
    onReturnClicked: () -> Unit,

    worldMapRaceViewModel: WorldMapRaceViewModel = hiltViewModel()
) {
    // Collect each UI state change.
    val worldMapRaceUiState by worldMapRaceViewModel.worldMapRaceUiState.collectAsState()
    // Collect each location update.
    val location: PlayerPosition? by worldMapRaceViewModel.currentLocation.collectAsState()
    // With each UI state, compose the race mode UI.
    RaceMode(
        raceMode = raceMode,
        currentLocation = location,
        worldMapRaceUiState = worldMapRaceUiState,
        componentActivity = LocalContext.current.getActivity()
    )
    // In a launched effect, set the targeted track UID.
    LaunchedEffect(key1 = Unit, block = {
        worldMapRaceViewModel.setTrackUid(trackUid)
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceMode(
    raceMode: WorldMapUiState.WorldMapLoadedRaceMode,
    currentLocation: PlayerPosition?,
    worldMapRaceUiState: WorldMapRaceUiState,
    componentActivity: ComponentActivity? = null
) {
    // Remember the last non-null location as a mutable state. The changing of which will cause recomposition.
    var lastLocation: PlayerPosition by remember { mutableStateOf<PlayerPosition>(currentLocation ?: raceMode.location) }
    // Remember a state for controlling whether camera should be following Player, or overviewing track. Default is false for when composition first performed.
    var shouldFollowPlayer by remember { mutableStateOf<Boolean>(false) }
    // Remember a state for a Track instance.
    var track by remember { mutableStateOf<Track?>(null) }
    // Remember a state for a Track Path instance.
    var trackPath by remember { mutableStateOf<TrackPath?>(null) }
    // Remember a camera position state to control the camera.
    val cameraPositionState = rememberCameraPositionState {
        // Center on the Player, with a close zoom such as 18f.
        /*position = CameraPosition.fromLatLngZoom(
            LatLng(lastLocation.latitude, lastLocation.longitude),
            18f
        )*/
        position = CameraPosition.builder()
            .target(LatLng(lastLocation.latitude, lastLocation.longitude))
            .zoom(18f)
            .tilt(0f)
            .bearing(lastLocation.rotation)
            .build()
    }
    // Update last location to current location if current location is not null.
    if(currentLocation != null) {
        // TODO: movement animation here for last location to current location.
        lastLocation = currentLocation
    }
    // Now, set our configuration from the current world map race UI state here.
    when(worldMapRaceUiState) {
        is WorldMapRaceUiState.Finished -> {
            /**
             * TODO: camera must be locked on device's location, 18f zoom, bearing following the device.
             * TODO: other UIs should include; showing the race's outcome relative to the current leaderboard; time achieved, percentage of track completed, checkpoints missed
             * TODO: and functionality to exit race mode. Screen around dialog showing results should be greyed out, and dialog can't be exited by tapping.
             */
            // Save both the track and track path.
            track = worldMapRaceUiState.track
            trackPath = worldMapRaceUiState.trackPath
            // We should not follow the Player, overview the track.
            shouldFollowPlayer = false
        }
        is WorldMapRaceUiState.Cancelled -> {
            /**
             * TODO: camera must be locked on device's location, 18f zoom, bearing following the device.
             * TODO: other UIs should include; showing the User they have cancelled the race. A button that will exit race mode. Screen around dialog should be greyed out,
             * TODO: and dialog can't be exited by tapping.
             */
            // Save both the track and track path.
            track = worldMapRaceUiState.track
            trackPath = worldMapRaceUiState.trackPath
            // We should not follow the Player, overview the track.
            shouldFollowPlayer = false
        }
        is WorldMapRaceUiState.Disqualified -> {
            /**
             * TODO: camera must be locked on device's location, 18f zoom, bearing following the device.
             * TODO: other UIs should include; showing the reason for disqualification, functionality to exit race mode. Screen around dialog should be greyed out, and dialog
             * TODO: can't be exited by tapping.
             */
            // Save both the track and track path.
            track = worldMapRaceUiState.track
            trackPath = worldMapRaceUiState.trackPath
            // We should not follow the Player, overview the track.
            shouldFollowPlayer = false
        }
        is WorldMapRaceUiState.Racing -> {
            /**
             * TODO: camera must be locked on device's location, 18f zoom, bearing following the device.
             * TODO: other UIs should include; the stopwatch, percentage of tracks left, number of checkpoints hit out of total required, function to cancel the race
             */
            // Save both the track and track path.
            track = worldMapRaceUiState.track
            trackPath = worldMapRaceUiState.trackPath
            // We should be following the Player.
            shouldFollowPlayer = true
        }
        is WorldMapRaceUiState.CountingDown -> {
            /**
             * TODO: camera must be locked on device's location, 18f zoom, bearing following the device.
             * TODO: other UIs should include; show the current seconds to GO, allow the User to cancel the countdown and therefore the race. Show a blank stopwatch.
             */
            // Save both the track and track path.
            track = worldMapRaceUiState.track
            trackPath = worldMapRaceUiState.trackPath
            // We should be following the Player.
            shouldFollowPlayer = true
        }
        is WorldMapRaceUiState.OnStartLine -> {
            /**
             * TODO: camera must be locked on device's location, 18f zoom, bearing following the device if our start line position is perfect, otherwise, camera should overview the track.
             * TODO: other UIs should include; the button for starting the race, within a modal bottom sheet. Perhaps some brief info about the track. If start line state
             * TODO: moves to MovedAway, this should invoke an exit from the race screen.
             */
            // Save both the track and track path.
            track = worldMapRaceUiState.track
            trackPath = worldMapRaceUiState.trackPath
            shouldFollowPlayer = when(worldMapRaceUiState.startLineState) {
                // When our position inconclusive, set follow player to false.
                is StartLineState.Inconclusive -> false
                // When our position is moved away, set follow player to false.
                is StartLineState.MovedAway -> false
                // When our position is perfect, set follow player to true.
                is StartLineState.Perfect -> true
                // When our position is standby, set follow player to false.
                is StartLineState.Standby -> false
            }
        }
        is WorldMapRaceUiState.CountdownDisqualified -> {
            // TODO
        }
        is WorldMapRaceUiState.Loading -> {
            /**
             * TODO: camera must be locked on device's location, 18f zoom, bearing following the device.
             * TODO: show a loading indicator in the center.
             */
        }
        is WorldMapRaceUiState.RaceStartFailed -> {
            /**
             * TODO: properly implement race start failing.
             */
            throw NotImplementedError("WorldMapRaceScreen failed because race failed to start, and this is not handled!")
        }
        is WorldMapRaceUiState.LoadFailed -> {
            /**
             * TODO: properly implement load failing.
             */
            throw NotImplementedError("WorldMapRaceScreen failed because load failed, and this is not handled!")
        }
    }

    Scaffold(
        // TODO: in this scaffold, we should have a modal-type bottom dialog fragment with controls on it. For now, we'll test with a
        // TODO low budget bottom app bar alongside buttons.
        bottomBar = {
            BottomAppBar {

            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Setup a Google map with all options set such that the Player can't adjust anything.
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    minZoomPreference = 18f,
                    maxZoomPreference = 18f,
                    mapStyleOptions = componentActivity?.let { activity ->
                        MapStyleOptions.loadRawResourceStyle(
                            activity,
                            R.raw.worldstyle
                        )
                    }
                ),
                uiSettings = MapUiSettings(
                    rotationGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    zoomControlsEnabled = false
                )
            ) {
                // Handle the camera here.
                if(shouldFollowPlayer) {
                    // If we should be following the Player, simply animate move the camera with the latest location.
                    // TODO: delete debug message.
                    Timber.d("Following player... We are moving the camera now.")
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLng(
                            LatLng(lastLocation.latitude, lastLocation.longitude)
                        )
                    )
                } else if(track != null && trackPath != null) {
                    Timber.d("Viewing track overview now...")
                    // Get the path's bounding box.
                    val trackBoundingBox = trackPath!!.getBoundingBox()
                    // Otherwise, if we have both track and track path, animate camera to view the entire track.
                    LaunchedEffect(key1 = track!!.trackUid, block = {
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
                } else {
                    // TODO: not following player and there is no track or no track path.
                    throw NotImplementedError("Failed to view world map race screen; not following player and there's no track or no track path. This is not handled.")
                }

                if(track != null && trackPath != null) {
                    // If we have both a track and a track path, draw it to the google map.
                    // TODO: review and centralise how we draw tracks.
                    track?.apply {
                        Marker(
                            state = MarkerState(
                                position = LatLng(
                                    startPoint.latitude,
                                    startPoint.longitude
                                )
                            ),
                            title = name,
                            snippet = description
                        )
                    }

                    trackPath?.apply {
                        DrawRaceTrack(
                            points = points.map { trackPoint ->
                                LatLng(trackPoint.latitude, trackPoint.longitude)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRaceMode(

) {
    HawkSpeedTheme {
        RaceMode(
            raceMode = WorldMapUiState.WorldMapLoadedRaceMode(
                "PLAYER01",
                GameSettings(true, null, null),
                PlayerPosition(0.0, 0.0, 0f, 0f, 0L),
                "TRACK01"
            ),
            currentLocation = PlayerPosition(0.0, 0.0, 0f, 0f, 0L),
            worldMapRaceUiState = WorldMapRaceUiState.Loading
        )
    }
}