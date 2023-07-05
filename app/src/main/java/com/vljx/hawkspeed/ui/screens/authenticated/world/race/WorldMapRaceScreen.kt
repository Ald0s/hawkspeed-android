package com.vljx.hawkspeed.ui.screens.authenticated.world.race

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import com.vljx.hawkspeed.Extension.FOLLOW_PLAYER_ZOOM
import com.vljx.hawkspeed.Extension.toFollowCameraUpdate
import com.vljx.hawkspeed.Extension.toOverviewCameraUpdate
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.Extension.toRaceTime
import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.ui.screens.dialogs.trackpreview.RaceModePromptUiState
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel.Companion.CANCELLED_BY_USER
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel.Companion.CANCEL_FALSE_START
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel.Companion.CANCEL_RACE_REASON_NO_LOCATION
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel.Companion.CANCEL_RACE_SERVER_REFUSED
import com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack.WorldMapRecordTrackUiState
import com.vljx.hawkspeed.ui.screens.common.BottomSheetTemporaryState
import com.vljx.hawkspeed.ui.screens.common.DrawCurrentPlayer
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrack
import com.vljx.hawkspeed.ui.screens.common.SheetControls
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData
import com.vljx.hawkspeed.util.Extension.getActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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
    // Now, set our configuration from the current world map race UI state here.
    when(worldMapRaceUiState) {
        is WorldMapRaceUiState.Finished -> {
            /**
             * TODO: camera must be locked on device's location, FOLLOW_PLAYER_ZOOM zoom, bearing following the device.
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
             * TODO: camera must be locked on device's location, FOLLOW_PLAYER_ZOOM zoom, bearing following the device.
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
             * TODO: camera must be locked on device's location, FOLLOW_PLAYER_ZOOM zoom, bearing following the device.
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
             * TODO: camera must be locked on device's location, FOLLOW_PLAYER_ZOOM zoom, bearing following the device.
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
             * TODO: camera must be locked on device's location, FOLLOW_PLAYER_ZOOM zoom, bearing following the device.
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
             * TODO: camera must be locked on device's location, FOLLOW_PLAYER_ZOOM zoom, bearing following the device if our start line position is perfect, otherwise, camera should overview the track.
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
             * TODO: camera must be locked on device's location, FOLLOW_PLAYER_ZOOM zoom, bearing following the device.
             * TODO: show a loading indicator in the center.
             */
            // We should be following the Player.
            shouldFollowPlayer = true
        }
        is WorldMapRaceUiState.RaceStartFailed -> {
            // Call our race start failed composable, which will render the issue to the User, and offer corrective action.
            RaceStartFailed(
                raceStartFailed = worldMapRaceUiState,
                onAcceptClicked = {
                    // TODO: we have accepted the race start failure issue.
                    throw NotImplementedError()
                }
            )
        }
        is WorldMapRaceUiState.LoadFailed -> {
            // Call our load failed composable, which will render the issue to the User.
            LoadFailed(
                loadFailed = worldMapRaceUiState,
                onAcceptClicked = {
                    // TODO: we have accepted the load failed issue.
                    throw NotImplementedError()
                }
            )
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
            // Call appropriate controls sheet for current state.
            when(worldMapRaceUiState) {
                is WorldMapRaceUiState.Cancelled ->
                    CancelledControls(
                        cancelled = worldMapRaceUiState,
                        scaffoldState = scaffoldState,
                        scope = scope,
                        onAcceptClicked = {
                            // TODO: accept cancellation clicked, exit race mode.
                        }
                    )
                is WorldMapRaceUiState.Disqualified ->
                    DisqualifiedControls(
                        disqualified = worldMapRaceUiState,
                        scaffoldState = scaffoldState,
                        scope = scope,
                        onAcceptClicked = {
                            // TODO: accept disqualification clicked. Was this only countdown? Remain in race mode. Had the race started? Exit race mode.
                        }
                    )
                is WorldMapRaceUiState.Finished ->
                    FinishedControls(
                        finished = worldMapRaceUiState,
                        scaffoldState = scaffoldState,
                        scope = scope,
                        onAcceptClicked = {
                            // TODO: good work! Accept race finished.
                        }
                    )
                is WorldMapRaceUiState.CountingDown ->
                    CountingDownControls(
                        countingDown = worldMapRaceUiState,
                        scaffoldState = scaffoldState,
                        scope = scope,
                        onCancelCountdownClicked = {
                            // TODO: cancel countdown
                        }
                    )
                is WorldMapRaceUiState.Racing ->
                    RacingControls(
                        racing = worldMapRaceUiState,
                        scaffoldState = scaffoldState,
                        scope = scope,
                        onCancelRaceClicked = {
                            // TODO: cancel race.
                        }
                    )
                is WorldMapRaceUiState.OnStartLine ->
                    StartLineControls(
                        startLine = worldMapRaceUiState,
                        scaffoldState = scaffoldState,
                        scope = scope,
                        onStartRaceClicked = { chosenVehicle, track, countdownPosition ->
                            // TODO: player wishes to begin a countdown.
                        }
                    )
                else -> { /* Nothing to do. */ }
            }
        }
    ) { paddingValues ->
        Box(
            contentAlignment = Alignment.Center,
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
                // Center on the Player, with a close zoom such as FOLLOW_PLAYER_ZOOM, as a default location.
                position = CameraPosition.fromLatLngZoom(
                    LatLng(lastLocation.latitude, lastLocation.longitude),
                    FOLLOW_PLAYER_ZOOM
                )
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings
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
                // If track is available, draw it to the map.
                track?.let {
                    DrawRaceTrack(
                        track = it,
                        trackPath = trackPath
                    )
                }
                // Start a new launched effect here that keys off should follow player and the track's path.
                LaunchedEffect(key1 = shouldFollowPlayer, key2 = trackPath, block = {
                    // If should follow player is false, animate camera position to overview the track. If should follow player is true, animate camera position
                    // to follow the Player.
                    if(shouldFollowPlayer) {
                        // Animate camera to last location.
                        cameraPositionState.animate(lastLocation.toFollowCameraUpdate(), 500)
                        mapProperties = mapProperties.copy(
                            minZoomPreference = FOLLOW_PLAYER_ZOOM,
                            maxZoomPreference = FOLLOW_PLAYER_ZOOM
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
                            cameraPositionState.animate(trackBoundingBox.toOverviewCameraUpdate(), 500)
                        }
                    } else {
                        // TODO: not following player and there is no track or no track path.
                        throw NotImplementedError("Failed to view world map race screen; not following player and there's no track or no track path. This is not handled.")
                    }
                })
                // Now, we'll handle moving the camera if we're following the User.
                if(shouldFollowPlayer) {
                    cameraPositionState.move(lastLocation.toFollowCameraUpdate())
                }
            }
            // If this is a counting down state, add a centered countdown second over the Google map.
            if(worldMapRaceUiState is WorldMapRaceUiState.CountingDown) {
                Text(
                    text = when(worldMapRaceUiState.currentSecond) {
                       4 -> stringResource(id = R.string.race_get_ready)
                        3, 2, 1 -> worldMapRaceUiState.currentSecond.toString()
                        0 -> stringResource(id = R.string.race_go)
                        else -> throw NotImplementedError()
                    },
                    modifier = Modifier
                        .zIndex(100f),
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 68.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisqualifiedControls(
    disqualified: WorldMapRaceUiState.Disqualified,

    onAcceptClicked: (() -> Unit)? = null,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    SheetControls(
        peekContent = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { }
                            .weight(1f)
                    ) {
                        Text(
                            text = disqualified.track.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        expandedContent = {

        }
    )
    // Disposable side effect to expand this sheet fully but only for the duration of this state.
    BottomSheetTemporaryState(
        desiredState = SheetValue.Expanded,
        sheetState = scaffoldState.bottomSheetState,
        scope = scope
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelledControls(
    cancelled: WorldMapRaceUiState.Cancelled,

    onAcceptClicked: (() -> Unit)? = null,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    SheetControls(
        peekContent = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { }
                            .weight(1f)
                    ) {
                        Text(
                            text = cancelled.track.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        expandedContent = { }
    )
    // Disposable side effect to expand this sheet fully but only for the duration of this state.
    BottomSheetTemporaryState(
        desiredState = SheetValue.Expanded,
        sheetState = scaffoldState.bottomSheetState,
        scope = scope
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishedControls(
    finished: WorldMapRaceUiState.Finished,

    onAcceptClicked: (() -> Unit)? = null,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    SheetControls(
        peekContent = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { }
                            .weight(1f)
                    ) {
                        Text(
                            text = finished.track.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        expandedContent = { }
    )
    // Disposable side effect to expand this sheet fully but only for the duration of this state.
    BottomSheetTemporaryState(
        desiredState = SheetValue.Expanded,
        sheetState = scaffoldState.bottomSheetState,
        scope = scope
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacingControls(
    racing: WorldMapRaceUiState.Racing,

    onCancelRaceClicked: ((Race) -> Unit)? = null,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    // We have a valid race instance here, from which we'll set up a stopwatch, using the provided started attribute as base.
    var currentRaceTime by remember { mutableStateOf<String>("00:00:000") }
    // Sheet controls.
    SheetControls(
        peekContent = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        // In racing mode, text will certainly be the time.
                        Text(
                            text = currentRaceTime,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                    }

                    Column {
                        Button(
                            onClick = {
                                // Cancel the race we're in.
                                onCancelRaceClicked?.invoke(racing.race)
                            },
                            enabled = true,
                            shape = RectangleShape,
                            modifier = Modifier
                                .wrapContentWidth()
                        ) {
                            Text(text = stringResource(id = R.string.race_cancel).uppercase())
                        }
                    }
                }
            }
        },
        expandedContent = { }
    )
    // A launched effect, keying off the current race's UID. This will run an infinite loop that will perform as our stopwatch.
    LaunchedEffect(key1 = racing.race.raceUid, block = {
        while(true) {
            // Subtract the current time milliseconds from the race started milliseconds to get back a duration.
            val deltaDuration = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
                .minus(racing.race.started.toDuration(DurationUnit.MILLISECONDS))
            // Set current race time to result of delta duration to race time.
            currentRaceTime = deltaDuration.toRaceTime()
            delay(20)
        }
    })
    // Disposable side effect to partially expand the sheet's state but only for the duration of this state.
    BottomSheetTemporaryState(
        desiredState = SheetValue.PartiallyExpanded,
        sheetState = scaffoldState.bottomSheetState,
        scope = scope
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountingDownControls(
    countingDown: WorldMapRaceUiState.CountingDown,

    onCancelCountdownClicked: (() -> Unit)? = null,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    SheetControls(
        peekContent = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { }
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.race_zero_time),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                    }

                    Column {
                        Button(
                            onClick = {
                                // TODO: cancel the race
                            },
                            enabled = true,
                            shape = RectangleShape,
                            modifier = Modifier
                                .wrapContentWidth()
                        ) {
                            Text(text = stringResource(id = R.string.race_countdown_cancel).uppercase())
                        }
                    }
                }
            }
        },
        expandedContent = { }
    )
    // Disposable side effect to partially expand the sheet's state but only for the duration of this state.
    BottomSheetTemporaryState(
        desiredState = SheetValue.PartiallyExpanded,
        sheetState = scaffoldState.bottomSheetState,
        scope = scope
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartLineControls(
    startLine: WorldMapRaceUiState.OnStartLine,

    onStartRaceClicked: ((Vehicle, Track, PlayerPosition) -> Unit)? = null,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    SheetControls(
        peekContent = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { }
                            .weight(1f)
                    ) {
                        Text(
                            text = startLine.track.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Button(
                            onClick = {
                                // TODO: start race.
                            },
                            enabled = true,
                            shape = RectangleShape,
                            modifier = Modifier
                                .wrapContentWidth()
                        ) {
                            Text(text = stringResource(id = R.string.race_start).uppercase())
                        }
                    }
                }
            }
        },
        expandedContent = { }
    )
    // Disposable side effect to partially expand the sheet's state but only for the duration of this state.
    BottomSheetTemporaryState(
        desiredState = SheetValue.PartiallyExpanded,
        sheetState = scaffoldState.bottomSheetState,
        scope = scope
    )
}

@Composable
fun RaceStartFailed(
    raceStartFailed: WorldMapRaceUiState.RaceStartFailed,

    onAcceptClicked: (() -> Unit)?
) {
    when(raceStartFailed.reasonCode) {
        CANCELLED_BY_USER -> throw NotImplementedError()
        CANCEL_FALSE_START -> throw NotImplementedError()
        CANCEL_RACE_SERVER_REFUSED -> throw NotImplementedError()
        CANCEL_RACE_REASON_NO_LOCATION -> throw NotImplementedError()
    }
}

@Composable
fun LoadFailed(
    loadFailed: WorldMapRaceUiState.LoadFailed,
    onAcceptClicked: (() -> Unit)?
) {

}

@Preview
@Composable
fun PreviewRaceMode(

) {
    HawkSpeedTheme {
        RaceMode(
            raceMode = WorldMapUiState.WorldMapLoadedRaceMode(
                "PLAYER01",
                GameSettings(true, null, null),
                PlayerPosition(0.0, 0.0, 0f, 0f, 0L),
                "YARRABOULEVARD"
            ),
            currentLocation = PlayerPosition(0.0, 0.0, 0f, 0f, 0L),
            worldMapRaceUiState = WorldMapRaceUiState.Disqualified(
                ExampleData.getExampleDisqualifiedRace(),
                track = ExampleData.getExampleTrack(),
                trackPath = ExampleData.getExampleTrackPath()
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewDisqualifiedControls(

) {
    HawkSpeedTheme {
        DisqualifiedControls(
            disqualified = WorldMapRaceUiState.Disqualified(
                race = ExampleData.getExampleDisqualifiedRace(),
                track = ExampleData.getExampleTrack(),
                trackPath = ExampleData.getExampleTrackPath()
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewCancelledControls(

) {
    HawkSpeedTheme {
        CancelledControls(
            cancelled = WorldMapRaceUiState.Cancelled(
                race = ExampleData.getExampleCancelledRace(),
                track = ExampleData.getExampleTrack(),
                trackPath = ExampleData.getExampleTrackPath()
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewFinishedControls(

) {
    HawkSpeedTheme {
        FinishedControls(
            finished = WorldMapRaceUiState.Finished(
                race = ExampleData.getExampleFinishedRace(),
                track = ExampleData.getExampleTrack(),
                trackPath = ExampleData.getExampleTrackPath()
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewRacingControls(

) {
    HawkSpeedTheme {
        RacingControls(
            racing = WorldMapRaceUiState.Racing(
                race = ExampleData.getExampleRacingRace(),
                track = ExampleData.getExampleTrack(),
                trackPath = ExampleData.getExampleTrackPath()
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewCountdownControls(

) {
    HawkSpeedTheme {
        CountingDownControls(
            countingDown = WorldMapRaceUiState.CountingDown(
                currentSecond = 0,
                countdownStartedLocation = PlayerPosition(0.0, 0.0, 0f, 0f, 0),
                track = ExampleData.getExampleTrack(),
                trackPath = ExampleData.getExampleTrackPath()
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewStartLineControls(

) {
    HawkSpeedTheme {
        StartLineControls(
            startLine = WorldMapRaceUiState.OnStartLine(
                yourVehicles = listOf(),
                startLineState = StartLineState.Perfect(PlayerPosition(0.0, 0.0, 0f, 0f, 0)),
                track = ExampleData.getExampleTrack(),
                trackPath = ExampleData.getExampleTrackPath()
            )
        )
    }
}

@Preview
@Composable
fun PreviewRaceStartFailed(

) {

}

@Preview
@Composable
fun PreviewLoadFailed(

) {

}

