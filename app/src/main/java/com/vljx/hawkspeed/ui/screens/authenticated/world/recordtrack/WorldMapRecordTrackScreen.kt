package com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.res.painterResource
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
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.Extension.FOLLOW_PLAYER_ZOOM
import com.vljx.hawkspeed.Extension.toFollowCameraUpdate
import com.vljx.hawkspeed.Extension.toOverviewCameraUpdate
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.world.DeviceOrientation
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState
import com.vljx.hawkspeed.ui.screens.common.BottomSheetTemporaryState
import com.vljx.hawkspeed.ui.screens.common.DrawCurrentPlayer
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrack
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrackDraft
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.screens.common.SheetControls
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData
import com.vljx.hawkspeed.util.Extension.getActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
fun WorldMapRecordTrackMode(
    recordTrackMode: WorldMapUiState.WorldMapLoadedRecordTrackMode,

    onSetupTrackDetails: ((TrackDraftWithPoints) -> Unit)? = null,
    onCancelRecordingClicked: (() -> Unit)? = null,

    worldMapRecordTrackViewModel: WorldMapRecordTrackViewModel = hiltViewModel()
) {
    // Collect world map record states.
    val worldMapRecordTrackUiState: WorldMapRecordTrackUiState by worldMapRecordTrackViewModel.recordTrackUiState.collectAsState()
    when(worldMapRecordTrackUiState) {
        is WorldMapRecordTrackUiState.RecordingComplete -> {
            // If recording complete, we'll move to the track detail UI, so call a loading screen composable.
            LoadingScreen()
            // In a launched effect, invoke the callback for setup track details. This is technically invoked by a flow, not a direct User action.
            LaunchedEffect(key1 = Unit, block = {
                onSetupTrackDetails?.invoke(
                    (worldMapRecordTrackUiState as WorldMapRecordTrackUiState.RecordingComplete).trackDraftWithPoints
                )
            })
        }
        is WorldMapRecordTrackUiState.RecordingCancelled -> {
            // If we have cancelled recording, call loading screen composable.
            LoadingScreen()
            // When recording has been cancelled, we can now exit back to standard mode.
            onCancelRecordingClicked?.invoke()
        }
        else -> {
            // Also recompose when current location changes.
            val currentLocationWithOrientation: PlayerPositionWithOrientation? by worldMapRecordTrackViewModel.currentLocationWithOrientation.collectAsState()
            // If current location is ever null, simply show the loading composable instead of the map.
            if(currentLocationWithOrientation != null) {
                // Create the google map.
                RecordTrack(
                    recordTrackMode,
                    currentLocationWithOrientation,
                    worldMapRecordTrackUiState,

                    onStartRecordingClicked = worldMapRecordTrackViewModel::startRecording,
                    onUseTrackClicked = { trackDraftWithPoints ->
                        worldMapRecordTrackViewModel.recordingComplete(trackDraftWithPoints)
                    },
                    onResetTrackClicked = worldMapRecordTrackViewModel::resetTrack,
                    onStopRecordingClicked = worldMapRecordTrackViewModel::stopRecording,
                    onCancelRecordingClicked = onCancelRecordingClicked,

                    componentActivity = LocalContext.current.getActivity()
                )
                LaunchedEffect(key1 = Unit, block = {
                    try {
                        // This is where we'd set the track draft's Id, if we're editing. But for now, just call new track.
                        /**
                         * TODO: this is where we'll also require the track type to be chosen. For now, we'll always use Sprint.
                         * TODO: this is actually where we should be passing a state that will function like a trigger; causing a request for a track type to create a new track.
                         */
                        worldMapRecordTrackViewModel.newTrack(
                            TrackType.SPRINT
                        )
                    } catch(ise: IllegalStateException) {
                        Timber.w("newTrack threw illegal state exception - new track not created.")
                    }
                })
            } else {
                // We do not have a current location for the Player just yet. Call loading composable.
                LoadingScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordTrack(
    recordTrackMode: WorldMapUiState.WorldMapLoadedRecordTrackMode,
    currentLocationWithOrientation: PlayerPositionWithOrientation?,
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
    var lastLocationWithOrientation: PlayerPositionWithOrientation by remember {
        mutableStateOf<PlayerPositionWithOrientation>(currentLocationWithOrientation ?: recordTrackMode.locationWithOrientation)
    }
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
        }
        is WorldMapRecordTrackUiState.RecordedTrackOverview -> {
            // Set latest track draft with points.
            trackDraftWithPoints = worldMapRecordTrackUiState.trackDraftWithPoints
            // View should no longer follow Player.
            shouldFollowPlayer = false
        }
        is WorldMapRecordTrackUiState.NewTrack -> {
            // Set latest track draft with points.
            trackDraftWithPoints = worldMapRecordTrackUiState.trackDraftWithPoints
            // Set view to be locked on to Player.
            shouldFollowPlayer = true
        }
        is WorldMapRecordTrackUiState.Loading -> {
            /**
             * TODO: set the view locked to following the player if location is available, otherwise, run another composable
             * TODO: that indicates location is being waited for.
             */
        }
        else -> { /* RecordingCancelled, NoSelectedTrackDraft, RecordingComplete not handled here, but in caller. */ }
    }

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            skipPartiallyExpanded = false,
            initialValue = SheetValue.Expanded
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetShadowElevation = 42.dp,
        sheetDragHandle = { /* No drag handle. */ },
        sheetSwipeEnabled = false,
        sheetContent = {
            // Call the most applicable content composable here for UI state.
            when(worldMapRecordTrackUiState) {
                is WorldMapRecordTrackUiState.RecordedTrackOverview ->
                    RecordedTrackOverviewControls(
                        recordedTrackOverview = worldMapRecordTrackUiState,

                        onUseTrackClicked = onUseTrackClicked,
                        onResetTrackClicked = onResetTrackClicked
                    )
                is WorldMapRecordTrackUiState.Recording ->
                    RecordingControls(
                        recording = worldMapRecordTrackUiState,

                        onStopRecordingClicked = onStopRecordingClicked
                    )
                is WorldMapRecordTrackUiState.NewTrack ->
                    NewTrackControls(
                        newTrack = worldMapRecordTrackUiState,

                        onStartRecordingClicked = onStartRecordingClicked,
                        onCancelRecordingClicked = onCancelRecordingClicked
                    )
                /**
                 * TODO: insert a new state in here that requests all information required to create a new track draft.
                 * For now, this is just the desired track type. This should then invoke a callback that will trigger the new track draft use case.
                 */
                else -> {
                    Column(modifier = Modifier.height(20.dp)) {

                    }
                }
            }
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
                    LatLng(recordTrackMode.locationWithOrientation.position.latitude, recordTrackMode.locationWithOrientation.position.longitude),
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
                // Start a new launched effect here that keys off should follow player.
                LaunchedEffect(key1 = shouldFollowPlayer, key2 = trackDraftWithPoints, block = {
                    if(shouldFollowPlayer || (trackDraftWithPoints?.hasRecordedTrack == false)) {
                        // Animate camera to last location.
                        cameraPositionState.animate(
                            lastLocationWithOrientation.toFollowCameraUpdate(),
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
    DisposableEffect(key1 = Unit, effect = {
        scope.launch {
            scaffoldState.bottomSheetState.expand()
        }
        onDispose {
            scope.launch {
                scaffoldState.bottomSheetState.hide()
            }
        }
    })
}

@Composable
fun RecordedTrackOverviewControls(
    recordedTrackOverview: WorldMapRecordTrackUiState.RecordedTrackOverview,

    onUseTrackClicked: ((TrackDraftWithPoints) -> Unit)? = null,
    onResetTrackClicked: (() -> Unit)? = null,

    contentPadding: PaddingValues = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
) {
    Surface {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.record_track_recorded),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = 12.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_question_circle), contentDescription = "track type")
                }
                Column(
                    modifier = Modifier
                ) {
                    Text(
                        text = when(recordedTrackOverview.trackDraftWithPoints.trackType) {
                            TrackType.SPRINT -> stringResource(id = R.string.track_type_sprint)
                            TrackType.CIRCUIT -> stringResource(id = R.string.track_type_circuit)
                            else -> throw NotImplementedError("Failed to set recorded track overview - track type text. The provided type is not supported.")
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = 12.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_ruler_horizontal), contentDescription = "track length")
                }
                Column {
                    Text(
                        text = stringResource(id = R.string.record_track_length, recordedTrackOverview.totalLength),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Row {
                Column {
                    Row(
                        modifier = Modifier
                            .padding(top = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Button(
                                onClick = {
                                    // Use track clicked, we'll move onto filling out details for it.
                                    onUseTrackClicked?.invoke(recordedTrackOverview.trackDraftWithPoints)
                                },
                                enabled = true,
                                shape = RectangleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(text = stringResource(id = R.string.record_use_track).uppercase())
                            }
                        }
                    }

                    Row {
                        Column(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .weight(1f)
                        ) {
                            TextButton(
                                onClick = {
                                    // Reset track clicked, this will delete the current track as its been recorded.
                                    onResetTrackClicked?.invoke()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(text = stringResource(id = R.string.record_reset_track).uppercase())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordingControls(
    recording: WorldMapRecordTrackUiState.Recording,

    onStopRecordingClicked: (() -> Unit)? = null,

    contentPadding: PaddingValues = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
) {
    Surface {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.record_track_recording),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Column {
                    Button(
                        onClick = {
                            // Stop recording clicked.
                            onStopRecordingClicked?.invoke()
                        },
                        enabled = true,
                        shape = RectangleShape,
                        modifier = Modifier
                            .wrapContentWidth()
                    ) {
                        Text(text = stringResource(id = R.string.record_stop_recording).uppercase())
                    }
                }
            }
        }
    }
}

@Composable
fun NewTrackControls(
    newTrack: WorldMapRecordTrackUiState.NewTrack,

    onStartRecordingClicked: (() -> Unit)? = null,
    onCancelRecordingClicked: (() -> Unit)? = null,

    contentPadding: PaddingValues = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
) {
    Surface {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.record_record_new_sprint),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Column {
                    Button(
                        onClick = {
                            // Start recording the track now.
                            onStartRecordingClicked?.invoke()
                        },
                        enabled = true,
                        shape = RectangleShape,
                        modifier = Modifier
                            .wrapContentWidth()
                    ) {
                        Text(text = stringResource(id = R.string.record_start_recording).uppercase())
                    }
                }
            }

            Row {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    TextButton(
                        onClick = {
                            // Cancel track recording/creation clicked, this will delete the track draft and quit.
                            onCancelRecordingClicked?.invoke()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.record_cancel).uppercase())
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTrackTypeControls(
    onTypeSelected: ((TrackType) -> Unit)? = null,

    contentPadding: PaddingValues = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
) {
    val options = listOf(
        R.string.record_track_choose_type_prompt,
        R.string.record_track_type_sprint
    ).map { stringResource(id = it) } // TODO: circuit removed R.string.record_track_type_circuit

    val descriptions = mapOf(
        options[0] to R.string.record_track_choose_type_prompt_desc,
        options[1] to R.string.record_track_type_sprint_desc
    ).map { Pair(it.key, stringResource(id = it.value)) }.toMap() // TODO: circuit removed options[2] to R.string.record_track_type_circuit_desc

    var selectedTrackType by remember { mutableStateOf<TrackType?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }
    var selectedOptionDescription by remember { mutableStateOf(descriptions[selectedOptionText] ?: "") }

    Surface {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            Row {
                Column {
                    Text(
                        text = stringResource(id = R.string.record_track_choose_type_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp)
            ) {
                Column {
                    Text(text = selectedOptionDescription)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    ExposedDropdownMenuBox(
                        modifier = Modifier
                            .fillMaxWidth(),
                        expanded = expanded,
                        onExpandedChange = {
                            expanded = !expanded
                        }
                    ) {
                        TextField(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            value = selectedOptionText,
                            onValueChange = { },
                            label = {
                                Text(text = stringResource(id = R.string.record_track_types))
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {
                            options.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = selectionOption)
                                    },
                                    onClick = {
                                        selectedOptionText = selectionOption
                                        selectedOptionDescription = descriptions[selectionOption] ?: ""
                                        expanded = false
                                        selectedTrackType = when(selectionOption) {
                                            options[0] -> null
                                            options[1] -> TrackType.SPRINT
                                            else -> null // TODO: removed circuit options[2] -> TrackType.CIRCUIT
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Row {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Button(
                        onClick = {
                            selectedTrackType?.let { type ->
                                onTypeSelected?.invoke(type)
                            }
                        },
                        enabled = selectedTrackType != null,
                        shape = RectangleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.record_track_choose_type).uppercase())
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewWorldMapRecordTrack(

) {
    HawkSpeedTheme {
        RecordTrack(
            recordTrackMode = WorldMapUiState.WorldMapLoadedRecordTrackMode(
                "PLAYER01",
                account = ExampleData.getExampleAccount(),
                GameSettings(true, null, null),
                locationWithOrientation = PlayerPositionWithOrientation(
                    PlayerPosition(0.0, 0.0, 0.0f, 0.0f, 0),
                    DeviceOrientation(FloatArray(3))
                )
            ),
            currentLocationWithOrientation = PlayerPositionWithOrientation(
                PlayerPosition(0.0, 0.0, 0.0f, 0.0f, 0),
                DeviceOrientation(FloatArray(3))
            ),
            WorldMapRecordTrackUiState.RecordedTrackOverview(
                TrackDraftWithPoints(
                    10L,
                    TrackType.SPRINT,
                    null,
                    null,
                    listOf()
                ),
                "2.4km"
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewRecordedTrackOverviewControls(

) {
    HawkSpeedTheme {
        RecordedTrackOverviewControls(
            recordedTrackOverview = WorldMapRecordTrackUiState.RecordedTrackOverview(
                ExampleData.getTrackDraftWithPoints(),
                "872m"
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewRecordingControls(

) {
    HawkSpeedTheme {
        RecordingControls(
            recording = WorldMapRecordTrackUiState.Recording(
                ExampleData.getTrackDraftWithPoints(),
                "872m"
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewNewTrackControls(

) {
    HawkSpeedTheme {
        NewTrackControls(
            newTrack = WorldMapRecordTrackUiState.NewTrack(
                ExampleData.getTrackDraftWithPoints()
            )
        )
    }
}

@Preview
@Composable
fun PreviewSelectTrackTypeControls(

) {
    HawkSpeedTheme {
        SelectTrackTypeControls()
    }
}