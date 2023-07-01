package com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrack
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.Extension.getActivity
import timber.log.Timber

/**
 * TODO: world map is engaged in record track mode. Call a composable that will set the world up to focus in on the player's current location, lock the camera
 * TODO to follow the User, and show related overlay UI that allows User to record, pause, stop, reset the recording - and allows the User to finalise and submit
 * TODO: the new track.
 */
@Composable
fun WorldMapRecordTrackMode(
    recordTrackMode: WorldMapUiState.WorldMapLoadedRecordTrackMode,
    onSetupTrackDetails: (TrackDraftWithPoints) -> Unit,
    onCancelRecordingClicked: () -> Unit,
    worldMapRecordTrackViewModel: WorldMapRecordTrackViewModel = hiltViewModel()
) {
    // Collect world map record states.
    val worldMapRecordTrackUiState: WorldMapRecordTrackUiState by worldMapRecordTrackViewModel.recordTrackUiState.collectAsState()
    when(worldMapRecordTrackUiState) {
        is WorldMapRecordTrackUiState.RecordingComplete -> {
            // In a launched effect, invoke the callback for setup track details. This is technically invoked by a flow, not a direct User action.
            LaunchedEffect(key1 = Unit, block = {
                onSetupTrackDetails(
                    (worldMapRecordTrackUiState as WorldMapRecordTrackUiState.RecordingComplete).trackDraftWithPoints
                )
            })
        }
        is WorldMapRecordTrackUiState.RecordingCancelled -> {
            // When recording has been cancelled, we can now exit back to standard mode.
            onCancelRecordingClicked()
        }
        is WorldMapRecordTrackUiState.NoSelectedTrackDraft -> {
            /**
             * TODO: I have created this state because I'm not entirely sure how to trigger the creation of a new track draft when none is provided for editing,
             * TODO: and at the same time, avoid the creation of a new track draft if the User decides to navigate back from setup track details to this screen;
             * TODO: which will cause a new track draft to be created if this side effect is launched at the bottom of composition ( which it was )
             */
            // Launch a new effect to create a new track.
            //LaunchedEffect(key1 = Unit, block = {
            // This is where we'd set the track draft's Id, if we're editing. But for now, just call new track.
            //    worldMapRecordTrackViewModel.newTrack()
            //})
        }
        else -> {
            // Collect location updates here.
            val currentLocation: PlayerPosition? by worldMapRecordTrackViewModel.currentLocation.collectAsState()
            // If current location is ever null, simply show the loading composable instead of the map.
            if(currentLocation != null) {
                // Create the google map.
                RecordTrack(
                    recordTrackMode,
                    currentLocation!!,
                    worldMapRecordTrackUiState,

                    onStartRecordingClicked = worldMapRecordTrackViewModel::startRecording,
                    onUseTrackClicked = { trackDraftWithPoints ->
                        worldMapRecordTrackViewModel.recordingComplete()
                    },
                    onResetTrackClicked = worldMapRecordTrackViewModel::resetTrack,
                    onStopRecordingClicked = worldMapRecordTrackViewModel::stopRecording,
                    onCancelRecordingClicked = onCancelRecordingClicked,
                    onMapClicked = {

                    },

                    componentActivity = LocalContext.current.getActivity()
                )
                // TODO: newTrack launchedeffect was here.
                // TODO: this may cause a new track every single time we navigate here; even if we navigate back from details.
                LaunchedEffect(key1 = Unit, block = {
                    // This is where we'd set the track draft's Id, if we're editing. But for now, just call new track.
                    worldMapRecordTrackViewModel.newTrack()
                })
            } else {
                // TODO: improve this.
                Loading()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordTrack(
    recordTrackMode: WorldMapUiState.WorldMapLoadedRecordTrackMode,
    currentLocation: PlayerPosition?,
    worldMapRecordTrackUiState: WorldMapRecordTrackUiState,
    onStartRecordingClicked: (() -> Unit)? = null,
    onUseTrackClicked: ((TrackDraftWithPoints) -> Unit)? = null,
    onResetTrackClicked: (() -> Unit)? = null,
    onStopRecordingClicked: (() -> Unit)? = null,
    onCancelRecordingClicked: (() -> Unit)? = null,
    onMapClicked: ((LatLng) -> Unit)? = null,
    componentActivity: ComponentActivity? = null
) {
    // Remember the last non-null location as a mutable state. The changing of which will cause recomposition.
    var lastLocation: PlayerPosition by remember { mutableStateOf<PlayerPosition>(currentLocation ?: recordTrackMode.location) }
    // Remember a boolean - when this is true, the view will follow the User. Otherwise, the view will overview the recorded track. But if that
    // too is not valid (trackDraftWithPoints is null, or there are no points in the track) the loading composable will be overlayed.
    var shouldFollowPlayer by remember { mutableStateOf<Boolean>(false) }
    // Remember a track draft with points - the most up to date track and its draft points.
    var trackDraftWithPoints by remember { mutableStateOf<TrackDraftWithPoints?>(null) }
    // Remember a camera position state for manipulating camera.
    val cameraPositionState = rememberCameraPositionState {
        // Center on the Player, with a close zoom such as 18f.
        /*position = CameraPosition.fromLatLngZoom(
            LatLng(currentLocation.latitude, currentLocation.longitude),
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

    when(worldMapRecordTrackUiState) {
        is WorldMapRecordTrackUiState.Recording -> {
            // Set latest track draft with points.
            trackDraftWithPoints = worldMapRecordTrackUiState.trackDraftWithPoints
            // Set view to be locked on to Player.
            shouldFollowPlayer = true
            /**
             * TODO: set the view locked to following the player.
             */
        }
        is WorldMapRecordTrackUiState.RecordedTrackOverview -> {
            // Set latest track draft with points.
            trackDraftWithPoints = worldMapRecordTrackUiState.trackDraftWithPoints
            // View should no longer follow Player.
            shouldFollowPlayer = false

            /**
             * TODO: set the view to surround the entire recorded map.
             */
        }
        is WorldMapRecordTrackUiState.NewTrack -> {
            // Set latest track draft with points.
            trackDraftWithPoints = worldMapRecordTrackUiState.trackDraftWithPoints
            // Set view to be locked on to Player.
            shouldFollowPlayer = true
            /**
             * TODO: set the view locked to following the player
             */
        }
        is WorldMapRecordTrackUiState.Loading -> {
            /**
             * TODO: set the view locked to following the player if location is available, otherwise, run another composable
             * TODO: that indicates location is being waited for.
             */
        }
        is WorldMapRecordTrackUiState.RecordingCancelled -> {
            // Handled in caller.
        }
        is WorldMapRecordTrackUiState.NoSelectedTrackDraft -> {
            // Handled in caller.
        }
        is WorldMapRecordTrackUiState.RecordingComplete -> {
            // Handled in caller.
        }
    }

    Scaffold(
        // TODO: in this scaffold, we should have a modal-type bottom dialog fragment with controls on it. For now, we'll test with a
        // TODO low budget bottom app bar alongside buttons.
        bottomBar = {
            BottomAppBar {
                // TODO: we need total distance for the track.
                if(worldMapRecordTrackUiState is WorldMapRecordTrackUiState.Recording) {
                    Button(onClick = { onStopRecordingClicked?.invoke() }) {
                        Text(text = "Stop Recording")
                    }

                    Button(onClick = { onCancelRecordingClicked?.invoke() }) {
                        Text(text = "Cancel")
                    }
                } else if(worldMapRecordTrackUiState is WorldMapRecordTrackUiState.RecordedTrackOverview) {
                    Button(
                        onClick = {
                            // Invoke on use track callback, which will cause recorded track to be used.
                            onUseTrackClicked?.let {
                                it(worldMapRecordTrackUiState.trackDraftWithPoints)
                            }
                        }
                    ) {
                        Text(text = "Use Track")
                    }

                    Button(onClick = { onResetTrackClicked?.invoke() }) {
                        Text(text = "Reset Track")
                    }

                    Button(onClick = { onCancelRecordingClicked?.invoke() }) {
                        Text(text = "Cancel")
                    }
                } else if(worldMapRecordTrackUiState is WorldMapRecordTrackUiState.NewTrack) {
                    Button(onClick = { onStartRecordingClicked?.invoke() }) {
                        Text(text = "Start Recording")
                    }

                    Button(onClick = { onCancelRecordingClicked?.invoke() }) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            // Setup a Google map with all options set such that the Player can't adjust anything.
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
                    rotationGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    zoomControlsEnabled = false
                ),
                onMapClick = { latLng ->
                    onMapClicked?.invoke(latLng)
                }
            ) {
                // Now, process the required position for the camera.
                if(shouldFollowPlayer) {
                    Timber.d("We should now be following the Player. Animate camera to current location, incl rotation.")
                } else if(trackDraftWithPoints != null && trackDraftWithPoints?.hasRecordedTrack == true) {
                    Timber.d("Not following Player. Instead, we should show recorded track overview.")
                } else {
                    // Some loading overlay.
                    Timber.d("Not following player, and have no recorded track. Perhaps just follow player.")
                }

                // If there is a track draft with points, draw it as a polyline to map.
                if(trackDraftWithPoints != null) {
                    trackDraftWithPoints?.apply {
                        DrawRaceTrack(
                            points = pointDrafts.map { trackPointDraft ->
                                LatLng(trackPointDraft.latitude, trackPointDraft.longitude)
                            }
                        )
                    }
                }
                /**
                 * TODO: when in the following modes; NewTrack, Recording; we must lock the view constantly following the Player.
                 * TODO: when in the following modes; Loading; we must have the few grayed out in place, with a loading indicator.
                 * TODO: when in the following modes; RecordedTrackOverview; we must have the view zoomed out, centered over the entire recorded track and locked there.
                 */
            }
        }
    }
}

@Composable
fun Loading(

) {
    /**
     * TODO: this should be an overlay over the google map, that is grayed out indicating disabled on the edges, and a loading indicator in the center.
     */
    CircularProgressIndicator()
}

@Preview(showBackground = true)
@Composable
fun PreviewWorldMapRecordTrack(

) {
    HawkSpeedTheme {
        RecordTrack(
            recordTrackMode = WorldMapUiState.WorldMapLoadedRecordTrackMode(
                "PLAYER01",
                GameSettings(true, null, null),
                PlayerPosition(0.0,0.0,0f,0f,0)
            ),
            currentLocation = PlayerPosition(0.0,0.0,0f,0f,0),
            WorldMapRecordTrackUiState.RecordedTrackOverview(
                TrackDraftWithPoints(
                    10L,
                    null,
                    null,
                    null,
                    listOf()
                )
            )
        )
    }
}