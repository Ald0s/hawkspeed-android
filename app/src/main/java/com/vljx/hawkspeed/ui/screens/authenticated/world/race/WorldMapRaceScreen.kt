package com.vljx.hawkspeed.ui.screens.authenticated.world.race

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.exc.race.NoTrackPathException
import com.vljx.hawkspeed.domain.exc.race.NoTrackPathException.Companion.NO_TRACK_PATH
import com.vljx.hawkspeed.domain.exc.race.StartRaceFailedException.Companion.REASON_ALREADY_IN_RACE
import com.vljx.hawkspeed.domain.exc.race.StartRaceFailedException.Companion.REASON_NO_COUNTDOWN_POSITION
import com.vljx.hawkspeed.domain.exc.race.StartRaceFailedException.Companion.REASON_NO_STARTED_POSITION
import com.vljx.hawkspeed.domain.exc.race.StartRaceFailedException.Companion.REASON_NO_TRACK_FOUND
import com.vljx.hawkspeed.domain.exc.race.StartRaceFailedException.Companion.REASON_NO_VEHICLE
import com.vljx.hawkspeed.domain.exc.race.StartRaceFailedException.Companion.REASON_NO_VEHICLE_UID
import com.vljx.hawkspeed.domain.exc.race.StartRaceFailedException.Companion.REASON_POSITION_NOT_SUPPORTED
import com.vljx.hawkspeed.domain.exc.race.StartRaceFailedException.Companion.REASON_TRACK_NOT_READY
import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.models.race.Race.Companion.DQ_REASON_DISCONNECTED
import com.vljx.hawkspeed.domain.models.race.Race.Companion.DQ_REASON_MISSED_TRACK
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

    onFinishedRace: ((Race) -> Unit)? = null,
    onExitRaceMode: (() -> Unit)? = null,

    worldMapRaceViewModel: WorldMapRaceViewModel = hiltViewModel()
) {
    // Collect each UI state change.
    val worldMapRaceUiState by worldMapRaceViewModel.worldMapRaceUiState.collectAsState()
    // The only UI state we'll handle this early is load failed; since failing any that constitutes load failed means we shouldn't really
    // display the race map anyway.
    when(worldMapRaceUiState) {
        is WorldMapRaceUiState.LoadFailed -> {
            // Call our load failed composable, which will render the issue to the User.
            LoadFailed(
                loadFailed = worldMapRaceUiState as WorldMapRaceUiState.LoadFailed,
                onAcceptClicked = {
                    // On accepting a load failed, exit race mode.
                    onExitRaceMode?.invoke()
                }
            )
        }
        else -> {
            // Otherwise, draw the actual race mode UI. Collect each location update.
            val location: PlayerPosition? by worldMapRaceViewModel.currentLocation.collectAsState()
            // With each UI state, compose the race mode UI.
            RaceMode(
                raceMode = raceMode,
                currentLocation = location,
                worldMapRaceUiState = worldMapRaceUiState,

                onFinishedRace = onFinishedRace,
                onStartRaceClicked = worldMapRaceViewModel::startRace,
                onCancelRaceClicked = worldMapRaceViewModel::cancelRace,
                onRequestResetRaceIntent = {
                    // Reset race intent to idle.
                    worldMapRaceViewModel.resetRaceIntent()
                },
                onExitRaceMode = onExitRaceMode,

                componentActivity = LocalContext.current.getActivity()
            )
        }
    }
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

    onFinishedRace: ((Race) -> Unit)? = null,
    onStartRaceClicked: ((Vehicle, Track, PlayerPosition) -> Unit)? = null,
    onCancelRaceClicked: (() -> Unit)? = null,
    onRequestResetRaceIntent: (() -> Unit)? = null,
    onExitRaceMode: (() -> Unit)? = null,

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
        else -> { /* Nothing to do. */ }
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
                            // Reset race intent to idle.
                            onRequestResetRaceIntent?.invoke()
                        }
                    )
                is WorldMapRaceUiState.Disqualified ->
                    DisqualifiedControls(
                        disqualified = worldMapRaceUiState,
                        scaffoldState = scaffoldState,
                        scope = scope,
                        onAcceptClicked = {
                            // Reset race intent to idle.
                            onRequestResetRaceIntent?.invoke()
                        }
                    )
                is WorldMapRaceUiState.Finished ->
                    FinishedControls(
                        finished = worldMapRaceUiState,
                        scaffoldState = scaffoldState,
                        scope = scope,
                        onAcceptClicked = {
                            // Accepting a finished race will exit race mode, but in a successful manner.
                            onFinishedRace?.invoke(worldMapRaceUiState.race)
                        }
                    )
                is WorldMapRaceUiState.CountingDown ->
                    CountingDownControls(
                        countingDown = worldMapRaceUiState,
                        scaffoldState = scaffoldState,
                        scope = scope,
                        onCancelCountdownClicked = {
                            onCancelRaceClicked?.invoke()
                        }
                    )
                is WorldMapRaceUiState.Racing ->
                    RacingControls(
                        racing = worldMapRaceUiState,
                        scaffoldState = scaffoldState,
                        scope = scope,
                        onCancelRaceClicked = {
                            onCancelRaceClicked?.invoke()
                        }
                    )
                is WorldMapRaceUiState.OnStartLine ->
                    StartLineControls(
                        startLine = worldMapRaceUiState,
                        scaffoldState = scaffoldState,
                        scope = scope,
                        onStartRaceClicked = { chosenVehicle, track, countdownPosition ->
                            onStartRaceClicked?.invoke(chosenVehicle, track, countdownPosition)
                        }
                    )
                is WorldMapRaceUiState.RaceStartFailed -> {
                    // Call our race start failed composable, which will render the issue to the User, and offer corrective action.
                    RaceStartFailedControls(
                        raceStartFailed = worldMapRaceUiState,
                        scaffoldState = scaffoldState,
                        scope = scope,
                        onAcceptClicked = {
                            // Reset the race intent state to Idle, which will trigger a revision of our current position.
                            onRequestResetRaceIntent?.invoke()
                        },
                        onExitRaceModeRequest = {
                            // If race started failed requests, inform calling composable we wish to exit race mode.
                            onExitRaceMode?.invoke()
                        }
                    )
                }
                else -> {
                    // In all other cases, setup a temporary sheet state for hidden.
                    BottomSheetTemporaryState(
                        desiredState = SheetValue.Hidden,
                        sheetState = scaffoldState.bottomSheetState,
                        scope = scope
                    )
                }
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

@Composable
fun LoadFailed(
    loadFailed: WorldMapRaceUiState.LoadFailed,

    onAcceptClicked: (() -> Unit)? = null
) {
    /**
     * TODO: load failed is called when the initial loading of required resources failed.
     * The following is a list of all current known failures that will cause this. All of these MUST be handled.
     *   1. Track resource failed to load; expect the ResourceError instance from the Resource to be passed.
     *   2. Track path is null, the Track probably doesn't even have a path; expect a GeneralError with message NO_TRACK_PATH and exception an instance of NoTrackPathException.
     *   3. Your vehicles failed to load; expect the ResourceError instance from the Resource to be passed.
     */
    throw NotImplementedError("WorldMapRaceScreen::LoadFailed is not implemented!")
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
        desiredState = SheetValue.Expanded,
        peekContent = {
            Column {
                Row(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.race_disqualified),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row {
                    Text(text = stringResource(id = R.string.race_disqualified_desc))
                }
            }
        },
        expandedContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(text = when(disqualified.race.disqualificationReason) {
                        DQ_REASON_DISCONNECTED -> stringResource(id = R.string.race_disqualified_disconnected)
                        DQ_REASON_MISSED_TRACK -> stringResource(id = R.string.race_disqualified_missed_track)
                        else -> stringResource(id = R.string.race_disqualified_unknown)
                    })
                }
                Row {
                    Button(
                        onClick = {
                            // TODO: accept/exit race mode.
                        },
                        enabled = true,
                        shape = RectangleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.accept).uppercase())
                    }
                }
            }
        },
        scaffoldState = scaffoldState,
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
        desiredState = SheetValue.Expanded,
        peekContent = {
            Column {
                Row(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.race_cancelled),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row {
                    Text(text = stringResource(id = R.string.race_cancelled_desc))
                }
            }
        },
        expandedContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Row {
                    Button(
                        onClick = {
                            // TODO: accept/exit race mode.
                        },
                        enabled = true,
                        shape = RectangleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.accept).uppercase())
                    }
                }
            }
        },
        scaffoldState = scaffoldState,
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
        desiredState = SheetValue.Expanded,
        peekContent = {
            Column {
                Row(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.race_finished),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                /**
                 * TODO: we should have access to the relevant leaderboard entry item here, which places the User somehwere
                 * TODO: on the leaderboard and allows us to calculate averages and cool stuff.
                 */
                Row {
                    Text(text = stringResource(id = R.string.race_finished_desc))
                }
            }
        },
        expandedContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                /**
                 * TODO: some detail about the race attempt.
                 */
                Row {
                    Button(
                        onClick = {

                        },
                        enabled = true,
                        shape = RectangleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.accept).uppercase())
                    }
                }
            }
        },
        scaffoldState = scaffoldState,
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
        desiredState = SheetValue.PartiallyExpanded,
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
                            style = MaterialTheme.typography.headlineMedium,
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

                Row {
                    Text(
                        text = stringResource(id = R.string.race_progress, racing.race.percentComplete!!),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        modifier = Modifier
                            .padding(top = 24.dp)
                    )
                }
            }
        },
        scaffoldState = scaffoldState,
        scope = scope
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
        desiredState = SheetValue.PartiallyExpanded,
        peekContent = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.race_zero_time),
                            style = MaterialTheme.typography.headlineMedium,
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

                Row {
                    Text(
                        text = stringResource(id = R.string.race_progress, "0"),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        modifier = Modifier
                            .padding(top = 24.dp)
                    )
                }
            }
        },
        scaffoldState = scaffoldState,
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
        desiredState = SheetValue.PartiallyExpanded,
        peekContent = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = startLine.track.name,
                            style = MaterialTheme.typography.headlineSmall,
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
        scaffoldState = scaffoldState,
        scope = scope
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceStartFailedControls(
    raceStartFailed: WorldMapRaceUiState.RaceStartFailed,

    onAcceptClicked: (() -> Unit)? = null,
    onExitRaceModeRequest: (() -> Unit)? = null,

    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val reasonDescription by remember { mutableStateOf<String?>(null) }
    when (raceStartFailed.reasonCode) {
        CANCELLED_BY_USER -> throw NotImplementedError()
        CANCEL_FALSE_START -> throw NotImplementedError()
        CANCEL_RACE_SERVER_REFUSED -> {
            if(raceStartFailed.resourceError is ResourceError.SocketError) {
                // Now within server refused, there will be a whole other section of potential errors, keying off the socket error's reason.
                val socketError: ResourceError.SocketError = raceStartFailed.resourceError
                when(socketError.reason) {
                    REASON_ALREADY_IN_RACE -> throw NotImplementedError()
                    REASON_POSITION_NOT_SUPPORTED -> throw NotImplementedError()
                    REASON_NO_COUNTDOWN_POSITION -> throw NotImplementedError()
                    REASON_NO_STARTED_POSITION -> throw NotImplementedError()
                    REASON_NO_TRACK_FOUND -> throw NotImplementedError()
                    REASON_TRACK_NOT_READY -> throw NotImplementedError()
                    REASON_NO_VEHICLE_UID -> throw NotImplementedError()
                    REASON_NO_VEHICLE -> throw NotImplementedError()
                }
            } else {
                throw NotImplementedError("Failed handle racestartfailed with reason code server_refused. An unhandled resource error type was presented: ${raceStartFailed.resourceError}")
            }
        }
        CANCEL_RACE_REASON_NO_LOCATION -> throw NotImplementedError()
    }

    SheetControls(
        desiredState = SheetValue.Expanded,
        peekContent = {
            Column {
                Row(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.race_start_failed),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row {
                    Text(text = stringResource(id = R.string.race_start_failed_desc))
                }
            }
        },
        expandedContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(text = reasonDescription ?: stringResource(id = R.string.race_start_failed_unknown))
                }
                Row {
                    Button(
                        onClick = {
                            // TODO: accept/exit race mode.
                        },
                        enabled = true,
                        shape = RectangleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.accept).uppercase())
                    }
                }
            }
        },
        scaffoldState = scaffoldState,
        scope = scope
    )
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
            worldMapRaceUiState = WorldMapRaceUiState.RaceStartFailed(
                REASON_ALREADY_IN_RACE, null
            )
        )
    }
}

@Preview
@Composable
fun PreviewLoadFailed(

) {
    HawkSpeedTheme {
        LoadFailed(
            loadFailed = WorldMapRaceUiState.LoadFailed(
                ResourceError.GeneralError(
                    NO_TRACK_PATH,
                    NoTrackPathException()
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewRaceStartFailedControls(

) {
    HawkSpeedTheme {
        RaceStartFailedControls(
            raceStartFailed = WorldMapRaceUiState.RaceStartFailed(
                reasonCode = CANCEL_FALSE_START,
                resourceError = null
            )
        )
    }
}

