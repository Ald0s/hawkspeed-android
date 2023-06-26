package com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldActionState
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme

@Composable
fun WorldMapRecordTrackMode(
    onSetupTrackDetails: (TrackDraftWithPoints) -> Unit,
    onStopRecordingTrack: (Long, Boolean) -> Unit,
    recordTrackMode: WorldMapUiState.WorldMapLoadedRecordTrackMode,
    worldMapRecordTrackViewModel: WorldMapRecordTrackViewModel = hiltViewModel()
) {
    val worldMapRecordTrackUiState: WorldMapRecordTrackUiState by worldMapRecordTrackViewModel.recordTrackUiState.collectAsState()
    when(worldMapRecordTrackUiState) {
        is WorldMapRecordTrackUiState.NewTrack -> {
            // TODO: The track presented is a new, blank track.
        }
        is WorldMapRecordTrackUiState.Recording -> {
            // TODO: The track is currently being recorded.
        }
        is WorldMapRecordTrackUiState.RecordedTrackOverview -> {
            // TODO: The track has been recorded. Present an overview of the entire track.
        }
        is WorldMapRecordTrackUiState.RecordingComplete -> {
            // In a launched effect, invoke the callback for setup track details. This is technically invoked by a flow, not a direct User action.
            LaunchedEffect(key1 = Unit, block = {
                onSetupTrackDetails(
                    (worldMapRecordTrackUiState as WorldMapRecordTrackUiState.RecordingComplete).trackDraftWithPoints
                )
            })
        }
        is WorldMapRecordTrackUiState.Loading -> {
            // Call out to the loading composable.
            Loading()
        }
        is WorldMapRecordTrackUiState.NoSelectedTrackDraft -> {
            /**
             * TODO: I have created this state because I'm not entirely sure how to trigger the creation of a new track draft when none is provided for editing,
             * TODO: and at the same time, avoid the creation of a new track draft if the User decides to navigate back from setup track details to this screen;
             * TODO: which will cause a new track draft to be created if this side effect is launched at the bottom of composition ( which it was )
             */
            // Launch a new effect to create a new track.
            LaunchedEffect(key1 = Unit, block = {
                // This is where we'd set the track draft's Id, if we're editing. But for now, just call new track.
                worldMapRecordTrackViewModel.newTrack()
            })
        }
    }
    // Create the google map.
    RecordTrack(
        onStartRecordingClicked = worldMapRecordTrackViewModel::startRecording,
        onUseTrackClicked = { trackDraftWithPoints ->
            worldMapRecordTrackViewModel.recordingComplete()
        },
        onResetTrackClicked = worldMapRecordTrackViewModel::resetTrack,
        onStopRecordingClicked = worldMapRecordTrackViewModel::stopRecording,
        recordTrackMode,
        worldMapRecordTrackUiState
    )
    // TODO: newTrack launchedeffect was here.
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordTrack(
    onStartRecordingClicked: () -> Unit,
    onUseTrackClicked: (TrackDraftWithPoints) -> Unit,
    onResetTrackClicked: () -> Unit,
    onStopRecordingClicked: () -> Unit,

    recordTrackMode: WorldMapUiState.WorldMapLoadedRecordTrackMode,
    worldMapRecordTrackUiState: WorldMapRecordTrackUiState
) {
    val cameraPositionState = rememberCameraPositionState {
        // Center on the Player, with a close zoom such as 18f.
        position = CameraPosition.fromLatLngZoom(
            LatLng(recordTrackMode.location.latitude, recordTrackMode.location.longitude),
            18f
        )
        // TODO: set the camera's bearing to match first location's bearing. View should be straight down.
    }

    Scaffold(
        // TODO: in this scaffold, we should have a modal-type bottom dialog fragment with controls on it. For now, we'll test with a
        // TODO low budget bottom app bar alongside buttons.
        bottomBar = {
            BottomAppBar {
                if(worldMapRecordTrackUiState is WorldMapRecordTrackUiState.Recording) {
                    Button(onClick = onStopRecordingClicked) {
                        Text(text = "Stop Recording")
                    }

                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "Cancel")
                    }
                } else if(worldMapRecordTrackUiState is WorldMapRecordTrackUiState.RecordedTrackOverview) {
                    Button(
                        onClick = {
                            // Invoke on use track callback, which will cause recorded track to be used.
                            onUseTrackClicked(
                                worldMapRecordTrackUiState.trackDraftWithPoints
                            )
                        }
                    ) {
                        Text(text = "Use Track")
                    }

                    Button(onClick = onResetTrackClicked) {
                        Text(text = "Reset Track")
                    }

                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "Cancel")
                    }
                } else if(worldMapRecordTrackUiState is WorldMapRecordTrackUiState.NewTrack) {
                    Button(onClick = onStartRecordingClicked) {
                        Text(text = "Start Recording")
                    }

                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Setup a Google map with all options set such that the Player can't adjust anything.
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    rotationGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    zoomControlsEnabled = false
                ),
                onMapLoaded = {
                    // Map is loaded.
                }
            ) {

            }
        }
    }
}

@Composable
fun Loading(

) {
    // TODO: improve this composable. For now, just a circular progress indicator.
    CircularProgressIndicator()
}

@Preview(showBackground = true)
@Composable
fun PreviewWorldMapRecordTrack(

) {
    HawkSpeedTheme {
        RecordTrack(
            onStartRecordingClicked = {},
            onUseTrackClicked = { trackDraftWithPoints -> },
            onResetTrackClicked = {},
            onStopRecordingClicked = {},
            WorldMapUiState.WorldMapLoadedRecordTrackMode("AA", GameSettings(true, null, null),
                PlayerPosition(0.0,0.0,0f,0f,0), 10L
            ),
            WorldMapRecordTrackUiState.RecordedTrackOverview(
                TrackDraftWithPoints(
                    10L,
                    null,
                    null,
                    listOf()
                )
            )
        )
    }
}