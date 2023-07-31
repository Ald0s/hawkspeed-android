package com.vljx.hawkspeed.ui.screens.authenticated.world.race

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.Extension.FOLLOW_PLAYER_ZOOM
import com.vljx.hawkspeed.Extension.toFollowCameraPosition
import com.vljx.hawkspeed.Extension.toOverviewCameraUpdate
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.data.models.SocketErrorWrapperModel
import com.vljx.hawkspeed.domain.Extension.toRaceTime
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
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.models.world.CurrentPlayer
import com.vljx.hawkspeed.domain.models.world.DeviceOrientation
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation
import com.vljx.hawkspeed.ui.component.mapoverlay.DrawCurrentPlayer
import com.vljx.hawkspeed.ui.component.mapoverlay.MapOverlay
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapUiState
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel.Companion.CANCELLED_BY_USER
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel.Companion.CANCEL_FALSE_START
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel.Companion.CANCEL_RACE_REASON_NO_LOCATION
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.WorldMapRaceViewModel.Companion.CANCEL_RACE_SERVER_REFUSED
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrack
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData
import com.vljx.hawkspeed.util.Extension.getActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * TODO: things still to fix/work on with respect to map overlays.
 * 1. Currently, we rotate the (already rotated) player overlay to ensure rotating the camera does not interfere with the bearing of that player; but we should instead mediate between the player's
 * rotation and the camera's rotation to find the correct angle such that irrespective of whether the player rotates or the camera rotates, we're viewing the correct angle of the player.
 * 2. We must draw tracks & track paths via overlay as well, since we want the enhanced abilities
 */
@Composable
fun WorldMapRaceMode(
    raceMode: WorldMapUiState.WorldMapLoadedRaceMode,
    trackUid: String,

    onFinishedRace: ((Race, RaceLeaderboard) -> Unit)? = null,
    onExitRaceMode: (() -> Unit)? = null,

    worldMapRaceViewModel: WorldMapRaceViewModel = hiltViewModel()
) {
    // Collect each UI state change.
    val worldMapRaceUiState by worldMapRaceViewModel.worldMapRaceUiState.collectAsStateWithLifecycle()
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
            // Collect all changes to current player.
            val currentPlayer: CurrentPlayer? by worldMapRaceViewModel.currentPlayer.collectAsStateWithLifecycle()
            // With each UI state, compose the race mode UI.
            RaceMode(
                currentPlayer = currentPlayer
                    ?: CurrentPlayer(raceMode.account, raceMode.gameSettings, raceMode.locationWithOrientation.position),
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
    currentPlayer: CurrentPlayer,
    worldMapRaceUiState: WorldMapRaceUiState,

    onFinishedRace: ((Race, RaceLeaderboard) -> Unit)? = null,
    onStartRaceClicked: ((Vehicle, Track, PlayerPositionWithOrientation) -> Unit)? = null,
    onCancelRaceClicked: (() -> Unit)? = null,
    onRequestResetRaceIntent: (() -> Unit)? = null,
    onExitRaceMode: (() -> Unit)? = null,

    componentActivity: ComponentActivity? = null
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            skipPartiallyExpanded = false,
            initialValue = SheetValue.Expanded
        )
    )
    // Remember a state for controlling whether camera should be following Player, or overviewing track. Default is false for when composition first performed.
    var shouldFollowPlayer by remember { mutableStateOf<Boolean>(false) }
    // If follow mode enabled, this camera position will be moved to.
    var followCameraPosition by remember {
        mutableStateOf<CameraPosition?>(null)
    }
    // Remember a state for a Track instance.
    var track by remember { mutableStateOf<Track?>(null) }
    // Remember a state for a Track Path instance.
    var trackPath by remember { mutableStateOf<TrackPath?>(null) }

    LaunchedEffect(key1 = worldMapRaceUiState, block = {
        // Now, set our configuration from the current world map race UI state here.
        when(worldMapRaceUiState) {
            is WorldMapRaceUiState.Finished -> {
                // Save both the track and track path.
                track = worldMapRaceUiState.track
                trackPath = worldMapRaceUiState.trackPath
                // We should not follow the Player, overview the track.
                shouldFollowPlayer = false
            }
            is WorldMapRaceUiState.Cancelled -> {
                // Save both the track and track path.
                track = worldMapRaceUiState.track
                trackPath = worldMapRaceUiState.trackPath
                // We should not follow the Player, overview the track.
                shouldFollowPlayer = false
            }
            is WorldMapRaceUiState.Disqualified -> {
                // Save both the track and track path.
                track = worldMapRaceUiState.track
                trackPath = worldMapRaceUiState.trackPath
                // We should not follow the Player, overview the track.
                shouldFollowPlayer = false
            }
            is WorldMapRaceUiState.Racing -> {
                // Save both the track and track path.
                track = worldMapRaceUiState.track
                trackPath = worldMapRaceUiState.trackPath
                // We should be following the Player.
                shouldFollowPlayer = true
            }
            is WorldMapRaceUiState.CountingDown -> {
                // Save both the track and track path.
                track = worldMapRaceUiState.track
                trackPath = worldMapRaceUiState.trackPath
                // We should be following the Player.
                shouldFollowPlayer = true
            }
            is WorldMapRaceUiState.OnStartLine -> {
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
                // We should be following the Player.
                shouldFollowPlayer = true
            }
            else -> { /* Nothing to do. */ }
        }
    })

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

    val cameraPositionState = rememberCameraPositionState {
        // Center on the Player, with a close zoom such as FOLLOW_PLAYER_ZOOM, as a default location.
        position = currentPlayer.playerPosition.toFollowCameraPosition()
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetShadowElevation = 42.dp,
        sheetDragHandle = { /* No drag handle. */ },
        sheetSwipeEnabled = false,
        sheetContent = {
            // Call appropriate controls sheet for current state.
            when(worldMapRaceUiState) {
                is WorldMapRaceUiState.Cancelled ->
                    CancelledControls(
                        cancelled = worldMapRaceUiState,
                        onAcceptClicked = {
                            // Reset race intent to idle.
                            onRequestResetRaceIntent?.invoke()
                        }
                    )
                is WorldMapRaceUiState.Disqualified ->
                    DisqualifiedControls(
                        disqualified = worldMapRaceUiState,
                        onAcceptClicked = {
                            // Reset race intent to idle.
                            onRequestResetRaceIntent?.invoke()
                        }
                    )
                is WorldMapRaceUiState.Finished ->
                    FinishedControls(
                        finished = worldMapRaceUiState,
                        onAcceptClicked = { race, raceLeaderboard ->
                            // Accepting a finished race will exit race mode, but in a successful manner.
                            onFinishedRace?.invoke(race, raceLeaderboard)
                        }
                    )
                is WorldMapRaceUiState.CountingDown ->
                    CountingDownControls(
                        countingDown = worldMapRaceUiState,
                        onCancelCountdownClicked = {
                            onCancelRaceClicked?.invoke()
                        }
                    )
                is WorldMapRaceUiState.Racing ->
                    RacingControls(
                        racing = worldMapRaceUiState,
                        onCancelRaceClicked = {
                            onCancelRaceClicked?.invoke()
                        }
                    )
                is WorldMapRaceUiState.OnStartLine -> {
                    // If our start line state is moved away, we must actually exit race mode.
                    if (worldMapRaceUiState.startLineState is StartLineState.MovedAway) {
                        // Call exit race mode and then loading screen composable.
                        onExitRaceMode?.invoke()
                        LoadingScreen()
                    } else {
                        StartLineControls(
                            startLine = worldMapRaceUiState,
                            onStartRaceClicked = { chosenVehicle, track, countdownPosition ->
                                onStartRaceClicked?.invoke(chosenVehicle, track, countdownPosition)
                            }
                        )
                    }
                }
                is WorldMapRaceUiState.RaceStartFailed -> {
                    // Call our race start failed composable, which will render the issue to the User, and offer corrective action.
                    RaceStartFailedControls(
                        raceStartFailed = worldMapRaceUiState,
                        onAcceptClicked = {
                            // Reset the race intent state to Idle, which will trigger a revision of our current position.
                            onRequestResetRaceIntent?.invoke()
                        }
                    )
                }
                else -> {
                    Column(modifier = Modifier.height(20.dp)) {

                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings
            ) {
                LaunchedEffect(
                    key1 = shouldFollowPlayer,
                    key2 = trackPath,
                    block = {
                        // If should follow player is false, animate camera position to overview the track. If should follow player is true, animate camera position
                        // to follow the Player.
                        if(shouldFollowPlayer) {
                            // Animate camera to last location.
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    currentPlayer.playerPosition.toFollowCameraPosition()
                                ),
                                500)
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
                    }
                )

                LaunchedEffect(key1 = followCameraPosition, block = {
                    if(shouldFollowPlayer && followCameraPosition != null && !cameraPositionState.isMoving) {
                        cameraPositionState.move(
                            CameraUpdateFactory.newCameraPosition(
                                followCameraPosition!!
                            )
                        )
                    }
                })

                track?.let {
                    DrawRaceTrack(
                        track = it,
                        trackPath = trackPath
                    )
                }
            }

            MapOverlay(
                cameraPositionState = cameraPositionState
            ) {
                DrawCurrentPlayer(
                    currentPlayer = currentPlayer,
                    isBeingFollowed = shouldFollowPlayer,
                    onNewCameraPosition = { latLng, rotation ->
                        followCameraPosition = PlayerPosition(latLng.latitude, latLng.longitude, rotation, 0f, 0L)
                            .toFollowCameraPosition()
                    }
                )
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

@Composable
fun DisqualifiedControls(
    disqualified: WorldMapRaceUiState.Disqualified,

    onAcceptClicked: (() -> Unit)? = null,

    contentPadding: PaddingValues = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
) {
    Surface {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.race_disqualified),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

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
    }
}

@Composable
fun CancelledControls(
    cancelled: WorldMapRaceUiState.Cancelled,

    onAcceptClicked: (() -> Unit)? = null,

    contentPadding: PaddingValues = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
) {
    Surface {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.race_cancelled),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(text = stringResource(id = R.string.race_cancelled_desc))
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
    }
}

@Composable
fun FinishedControls(
    finished: WorldMapRaceUiState.Finished,

    onAcceptClicked: ((Race, RaceLeaderboard) -> Unit)? = null,

    contentPadding: PaddingValues = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
) {
    Surface {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
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
            Row(
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(text = stringResource(id = R.string.race_finished_desc))
            }

            /**
             * TODO: some detail about the race attempt.
             */
            Row {
                Button(
                    onClick = {
                        // Accept race completion with the race and leaderboard entry.
                        onAcceptClicked?.invoke(finished.race, finished.raceLeaderboard)
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
    }
}

@Composable
fun RacingControls(
    racing: WorldMapRaceUiState.Racing,

    onCancelRaceClicked: ((Race) -> Unit)? = null,

    contentPadding: PaddingValues = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
) {
    // We have a valid race instance here, from which we'll set up a stopwatch, using the provided started attribute as base.
    var currentRaceTime by remember { mutableStateOf<String>("00:00:000") }

    Surface {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    ) {
                        // In racing mode, text will certainly be the time.
                        Text(
                            text = currentRaceTime,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                    }
                    Row {
                        Text(
                            text = stringResource(id = R.string.race_progress, racing.race.percentComplete!!),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
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
    }
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

@Composable
fun CountingDownControls(
    countingDown: WorldMapRaceUiState.CountingDown,

    onCancelCountdownClicked: (() -> Unit)? = null,

    contentPadding: PaddingValues = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
) {
    Surface {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    ) {
                        // In racing mode, text will certainly be the time.
                        Text(
                            text = stringResource(id = R.string.race_zero_time),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                    }
                    Row {
                        Text(
                            text = stringResource(id = R.string.race_progress, 0),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Column {
                    Button(
                        onClick = {
                            // Cancel the race we're in.
                            onCancelCountdownClicked?.invoke()
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
    }
}

@Composable
fun StartLineControls(
    startLine: WorldMapRaceUiState.OnStartLine,

    onStartRaceClicked: ((Vehicle, Track, PlayerPositionWithOrientation) -> Unit)? = null,

    contentPadding: PaddingValues = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
) {
    /**
     * TODO: player should choose their vehicle here. For now, the first will just be used.
     */
    var chosenVehicle: Vehicle by remember { mutableStateOf(startLine.yourVehicles.first()) }
    var canStartRace: Boolean by remember { mutableStateOf(startLine.startLineState is StartLineState.Perfect) }

    Surface {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 32.dp)
            ) {
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
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.race_your_vehicle)
                                )
                            }
                            Row {
                                Text(
                                    text = chosenVehicle.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Column {
                            TextButton(
                                onClick = {
                                    // TODO: User wishes to select another vehicle to race with.
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.race_change_your_vehicle)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if(canStartRace) {
                                // If can start race is true, we MUST have a location so not-null assert is OK.
                                onStartRaceClicked?.invoke(chosenVehicle, startLine.track, (startLine.startLineState as StartLineState.Perfect).location)
                            }
                        },
                        enabled = canStartRace,
                        shape = RectangleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.race_start).uppercase())
                    }
                }
            }
        }
    }
}

@Composable
fun RaceStartFailedControls(
    raceStartFailed: WorldMapRaceUiState.RaceStartFailed,

    onAcceptClicked: (() -> Unit)? = null,

    contentPadding: PaddingValues = PaddingValues(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
) {
    var reasonDescription by remember { mutableStateOf<String?>(null) }
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
                    REASON_TRACK_NOT_READY -> {
                        reasonDescription = stringResource(id = R.string.race_start_failed_cant_be_raced)
                    }
                    REASON_NO_VEHICLE_UID -> throw NotImplementedError()
                    REASON_NO_VEHICLE -> throw NotImplementedError()
                }
            } else {
                throw NotImplementedError("Failed handle racestartfailed with reason code server_refused. An unhandled resource error type was presented: ${raceStartFailed.resourceError}")
            }
        }
        CANCEL_RACE_REASON_NO_LOCATION -> throw NotImplementedError()
    }

    Surface {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
        ) {
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

            Row(
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = reasonDescription ?: stringResource(id = R.string.race_start_failed_unknown)
                )
            }
            Row {
                Button(
                    onClick = {
                        onAcceptClicked?.invoke()
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
    }
}

@Preview
@Composable
fun PreviewRaceMode(

) {
    HawkSpeedTheme {
        RaceMode(
            currentPlayer = CurrentPlayer(
                ExampleData.getExampleAccount(),
                GameSettings(true, null, null),
                PlayerPosition(0.0, 0.0, 0.0f, 0.0f, 0)
            ),
            worldMapRaceUiState = WorldMapRaceUiState.OnStartLine(
                listOf(
                    ExampleData.getExampleVehicle()
                ),
                StartLineState.Perfect(
                    PlayerPositionWithOrientation(
                        PlayerPosition(0.0, 0.0, 0.0f, 0.0f, 0),
                        DeviceOrientation(FloatArray(3))
                    )
                ),
                ExampleData.getExampleTrack(),
                ExampleData.getExampleTrackPath()
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
                raceLeaderboard = ExampleData.getExampleRaceLeaderboard(),
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
                countdownStartedLocationWithOrientation = PlayerPositionWithOrientation(
                    PlayerPosition(0.0, 0.0, 0.0f, 0.0f, 0),
                    DeviceOrientation(FloatArray(3))
                ),
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
                yourVehicles = listOf(
                    ExampleData.getExampleVehicle()
                ),
                startLineState = StartLineState.Perfect(PlayerPositionWithOrientation(
                    PlayerPosition(0.0, 0.0, 0.0f, 0.0f, 0),
                    DeviceOrientation(FloatArray(3))
                )),
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
                reasonCode = CANCEL_RACE_SERVER_REFUSED,
                resourceError = ResourceError.SocketError(
                    SocketErrorWrapperModel(
                        "start-race-fail",
                        REASON_TRACK_NOT_READY,
                        hashMapOf()
                    )
                )
            )
        )
    }
}

