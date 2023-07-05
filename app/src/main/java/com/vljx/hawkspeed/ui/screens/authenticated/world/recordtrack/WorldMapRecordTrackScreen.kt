package com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.Extension.FOLLOW_PLAYER_ZOOM
import com.vljx.hawkspeed.Extension.toFollowCameraUpdate
import com.vljx.hawkspeed.Extension.toOverviewCameraUpdate
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState
import com.vljx.hawkspeed.ui.screens.common.DrawCurrentPlayer
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrack
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrackDraft
import com.vljx.hawkspeed.ui.screens.common.SheetControls
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.Extension.getActivity
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

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
    var shouldFollowPlayer by remember { mutableStateOf<Boolean>(true) }
    // Remember a track draft with points - the most up to date track and its draft points.
    var trackDraftWithPoints by remember { mutableStateOf<TrackDraftWithPoints?>(null) }

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

    val sheetPeekHeight = 128
    val sheetContentPadding = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            skipPartiallyExpanded = false,
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = (sheetPeekHeight + sheetContentPadding.calculateBottomPadding().value).dp,
        sheetShadowElevation = 42.dp,
        sheetDragHandle = { /* No drag handle. */ },
        sheetSwipeEnabled = false,
        sheetContent = {
            // Call the most applicable content composable here for UI state.
            // TODO
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            var uiSettings by remember {
                mutableStateOf(MapUiSettings(
                    compassEnabled = false,
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
                    minZoomPreference = 3.0f,
                    maxZoomPreference = 21.0f,
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
            // Remember a camera position state for manipulating camera.
            val cameraPositionState = rememberCameraPositionState {
                // Center on the Player, with a close zoom such as FOLLOW_PLAYER_ZOOM, as a default location.
                position = CameraPosition.fromLatLngZoom(
                    LatLng(lastLocation.latitude, lastLocation.longitude),
                    FOLLOW_PLAYER_ZOOM
                )
            }
            // Setup a Google map with all options set such that the Player can't adjust anything.
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings,
                onMapClick = { latLng ->
                    onMapClicked?.invoke(latLng)
                }
            ) {
                if(currentLocation != null) {
                    // Draw the current Player to the map.
                    DrawCurrentPlayer(
                        newPlayerPosition = currentLocation,
                        oldPlayerPosition = lastLocation,
                        isFollowing = shouldFollowPlayer
                    )
                    lastLocation = currentLocation
                }
                // Start a new launched effect here that keys off should follow player.
                LaunchedEffect(key1 = shouldFollowPlayer, key2 = trackDraftWithPoints, block = {
                    if(shouldFollowPlayer || (trackDraftWithPoints?.hasRecordedTrack == false)) {
                        // Animate camera to last location.
                        cameraPositionState.animate(
                            lastLocation.toFollowCameraUpdate(),
                            500
                        )
                        mapProperties = mapProperties.copy(
                            minZoomPreference = FOLLOW_PLAYER_ZOOM,
                            maxZoomPreference = FOLLOW_PLAYER_ZOOM
                        )
                    } else if(trackDraftWithPoints != null) {
                        mapProperties = mapProperties.copy(
                            minZoomPreference = 3.0f,
                            maxZoomPreference = 21.0f
                        )
                        trackDraftWithPoints?.let {
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
                trackDraftWithPoints?.let {
                    DrawRaceTrackDraft(trackDraftWithPoints = it)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordedTrackOverviewControls(
    recordedTrackOverview: WorldMapRecordTrackUiState.RecordedTrackOverview,

    onUseTrackClicked: ((TrackDraftWithPoints) -> Unit)? = null,
    onResetTrackClicked: (() -> Unit)? = null,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    /**
     * TODO: recorded track overview should be a partially expanded controls sheet, with buttons 'Use Track' and 'Reset Track', also some summary information about
     * TODO: the new track like; num points, length etc?
     */
    SheetControls(
        peekContent = {

        },
        expandedContent = { }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingControls(
    recording: WorldMapRecordTrackUiState.Recording,

    onStopRecordingClicked: (() -> Unit)? = null,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    /**
     * TODO: recording controls should be a partially expanded controls sheet, with buttons 'Stop Recording'. Also some ongoing summary info about the track being
     * TODO: recorded.
     */
    SheetControls(
        peekContent = {

        },
        expandedContent = { }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTrackControls(
    newTrack: WorldMapRecordTrackUiState.NewTrack,

    onStartRecordingClicked: (() -> Unit)? = null,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    /**
     * TODO: new track controls should be a partially expanded controls sheet, with buttons 'Start Recording'.
     */
    SheetControls(
        peekContent = {

        },
        expandedContent = { }
    )
}

@Composable
fun Loading(

) {
    /**
     * TODO: this should be an overlay over the google map, that is grayed out indicating disabled on the edges, and a loading indicator in the center.
     */
    CircularProgressIndicator()
}

@Preview
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

@Preview
@Composable
fun PreviewRecordedTrackOverviewControls(

) {

}

@Preview
@Composable
fun PreviewRecordingControls(

) {

}

@Preview
@Composable
fun PreviewNewTrackControls(

) {

}