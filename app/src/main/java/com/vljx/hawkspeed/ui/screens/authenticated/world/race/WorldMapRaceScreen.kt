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
import com.vljx.hawkspeed.Extension.toFollowCameraUpdate
import com.vljx.hawkspeed.Extension.toOverviewCameraUpdate
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel.Companion.CANCELLED_BY_USER
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel.Companion.CANCEL_FALSE_START
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel.Companion.CANCEL_RACE_REASON_NO_LOCATION
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel.Companion.CANCEL_RACE_SERVER_REFUSED
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

        onStartRaceClicked = worldMapRaceViewModel::startRace,
        onCancelRaceClicked = worldMapRaceViewModel::cancelRace,

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

    onStartRaceClicked: ((Vehicle, Track, PlayerPosition) -> Unit)? = null,
    onCancelRaceClicked: (() -> Unit)? = null,

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
    // Update last location to current location if current location is not null.
    if(currentLocation != null) {
        // TODO: marker movement animation here for last location to current location.
        lastLocation = currentLocation
    }
    // Now, set our configuration from the current world map race UI state here.
    when(worldMapRaceUiState) {
        is WorldMapRaceUiState.Finished -> {
            /**
             * TODO: camera must be locked on device's location, 20f zoom, bearing following the device.
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
             * TODO: camera must be locked on device's location, 20f zoom, bearing following the device.
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
             * TODO: camera must be locked on device's location, 20f zoom, bearing following the device.
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
             * TODO: camera must be locked on device's location, 20f zoom, bearing following the device.
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
             * TODO: camera must be locked on device's location, 20f zoom, bearing following the device.
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
             * TODO: camera must be locked on device's location, 20f zoom, bearing following the device if our start line position is perfect, otherwise, camera should overview the track.
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
        is WorldMapRaceUiState.Loading -> {
            /**
             * TODO: camera must be locked on device's location, 20f zoom, bearing following the device.
             * TODO: show a loading indicator in the center.
             */
            // We should be following the Player.
            shouldFollowPlayer = true
        }
        is WorldMapRaceUiState.RaceStartFailed -> {
            /**
             * TODO: when a race fails to start, a dialog should be shown that notifies the user as to why, and presents a button that when clicked, will reset the
             * TODO: new race intent back to idle in view model.
             */
            when(worldMapRaceUiState.reasonCode) {
                CANCELLED_BY_USER -> throw NotImplementedError()
                CANCEL_FALSE_START -> throw NotImplementedError()
                CANCEL_RACE_SERVER_REFUSED -> throw NotImplementedError()
                CANCEL_RACE_REASON_NO_LOCATION -> throw NotImplementedError()
            }
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
                // TODO: we need total distance for the track.
                if(worldMapRaceUiState is WorldMapRaceUiState.Racing) {
                    Button(onClick = { onCancelRaceClicked?.invoke() }) {
                        Text(text = "Cancel Race")
                    }
                } else if(worldMapRaceUiState is WorldMapRaceUiState.CountingDown) {
                    Button(onClick = { onCancelRaceClicked?.invoke() }) {
                        Text(text = "Cancel Race Start")
                    }
                    Text(text = "COUNTDOWN: ${worldMapRaceUiState.currentSecond}")
                } else if(worldMapRaceUiState is WorldMapRaceUiState.OnStartLine) {
                    if(worldMapRaceUiState.startLineState is StartLineState.Perfect) {
                        Button(
                            onClick = {
                                // TODO: here we just select the very first vehicle, though we should actually be requesting it from the User.
                                onStartRaceClicked?.invoke(
                                    worldMapRaceUiState.yourVehicles.first(),
                                    worldMapRaceUiState.track,
                                    worldMapRaceUiState.startLineState.location
                                )
                            },
                            enabled = true
                        ) {
                            Text(text = "Start Race")
                        }
                    } else if(worldMapRaceUiState.startLineState is StartLineState.Standby) {
                        Button(
                            onClick = { },
                            enabled = false
                        ) {
                            Text(text = "Start Race")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            var uiSettings by remember {
                mutableStateOf(MapUiSettings(
                    myLocationButtonEnabled = false,
                    indoorLevelPickerEnabled = false,
                    mapToolbarEnabled = false,
                    rotationGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    zoomControlsEnabled = false
                ))
            }
            var mapProperties by remember {
                mutableStateOf(MapProperties(
                    isBuildingEnabled = false,
                    isIndoorEnabled = false,
                    isMyLocationEnabled = false,
                    minZoomPreference = 3.0f,
                    maxZoomPreference = 21.0f,
                    mapStyleOptions = componentActivity?.let { activity ->
                        MapStyleOptions.loadRawResourceStyle(
                            activity,
                            R.raw.worldstyle
                        )
                    }
                ))
            }
            // Remember a camera position state to control the camera.
            val cameraPositionState = rememberCameraPositionState {
                // Center on the Player, with a close zoom such as 20f, as a default location.
                position = CameraPosition.fromLatLngZoom(
                    LatLng(lastLocation.latitude, lastLocation.longitude),
                    20f
                )
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings
            ) {
                // Start a new launched effect here that keys off should follow player.
                LaunchedEffect(key1 = shouldFollowPlayer, key2 = trackPath, block = {
                    // If should follow player is false, animate camera position to overview the track. If should follow player is true, animate camera position
                    // to follow the Player.
                    if(shouldFollowPlayer) {
                        // Animate camera to last location.
                        cameraPositionState.animate(
                            lastLocation.toFollowCameraUpdate(),
                            500
                        )
                        mapProperties = mapProperties.copy(
                            minZoomPreference = 20f,
                            maxZoomPreference = 20f
                        )
                    } else if(trackPath != null) {
                        mapProperties = mapProperties.copy(
                            minZoomPreference = 3.0f,
                            maxZoomPreference = 21.0f
                        )
                        trackPath?.let {
                            // Get bounding box for track.
                            val trackBoundingBox = it.getBoundingBox()
                            // Animate camera to overview the track's path.
                            cameraPositionState.animate(
                                trackBoundingBox.toOverviewCameraUpdate(),
                                500
                            )
                        }
                    } else {
                        // TODO: not following player and there is no track or no track path.
                        throw NotImplementedError("Failed to view world map race screen; not following player and there's no track or no track path. This is not handled.")
                    }
                })

                // Now, we'll handle moving the camera if we're following the User.
                if(shouldFollowPlayer) {
                    // If we should be following the Player, set min and max zoom to 20f, tilt to 0, move camera over the Player and bearing to follow them.
                    cameraPositionState.move(
                        lastLocation.toFollowCameraUpdate()
                    )
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