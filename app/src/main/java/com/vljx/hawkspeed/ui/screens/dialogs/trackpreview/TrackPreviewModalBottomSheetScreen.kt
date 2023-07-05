package com.vljx.hawkspeed.ui.screens.dialogs.trackpreview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.ui.screens.authenticated.trackdetail.TrackRatingUiState
import com.vljx.hawkspeed.ui.screens.authenticated.world.race.StartLineState
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackPreviewModalBottomSheetScreen(
    track: Track,
    onRaceModeClicked: ((Track) -> Unit)? = null,
    onViewTrackDetailClicked: ((Track) -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,

    sheetState: SheetState = rememberModalBottomSheetState(),
    trackPreviewViewModel: TrackPreviewViewModel = hiltViewModel(),
) {
    val trackPreviewUiState: TrackPreviewUiState by trackPreviewViewModel.trackPreviewUiState.collectAsState()

    // Call the bottom sheet composable to set that up.
    TrackPreviewModalBottomSheet(
        trackPreviewUiState = trackPreviewUiState,
        onRaceModeClicked = onRaceModeClicked,
        onViewTrackDetailClicked = onViewTrackDetailClicked,
        onDismiss = onDismiss
    )
    // Build a launched effect here keying off the track's UID that will select that as the latest track in view model, so this will only
    // be called on the first composition, or if the track selected changes.
    LaunchedEffect(key1 = track.trackUid, block = {
        trackPreviewViewModel.selectTrack(track.trackUid)
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackPreviewModalBottomSheet(
    trackPreviewUiState: TrackPreviewUiState,
    modifier: Modifier = Modifier,
    onRaceModeClicked: ((Track) -> Unit)? = null,
    onViewTrackDetailClicked: ((Track) -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,

    sheetState: SheetState = rememberModalBottomSheetState(),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    ModalBottomSheet(
        sheetState = sheetState,
        scrimColor = Color.Transparent,
        dragHandle = { },
        onDismissRequest = {
            onDismiss?.invoke()
        }
    ) {
        TrackPreview(
            trackPreviewUi = trackPreviewUiState,
            onRaceModeClicked = onRaceModeClicked,
            onViewTrackDetailClicked = onViewTrackDetailClicked,
            modifier = Modifier
                .padding(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        )
    }
}

@Composable
fun TrackPreview(
    trackPreviewUi: TrackPreviewUiState,
    modifier: Modifier = Modifier,
    onRaceModeClicked: ((Track) -> Unit)? = null,
    onViewTrackDetailClicked: ((Track) -> Unit)? = null
) {
    when(trackPreviewUi) {
        is TrackPreviewUiState.TrackPreview -> {
            val track = trackPreviewUi.track
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .height(96.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { onViewTrackDetailClicked?.invoke(track) }
                            .weight(1f)
                    ) {
                        Text(
                            text = track.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Button(
                            onClick = {
                                onRaceModeClicked?.invoke(track)
                            },
                            enabled = trackPreviewUi.raceModePromptUiState is RaceModePromptUiState.CanEnterRaceMode,
                            shape = RectangleShape,
                            modifier = Modifier
                                .wrapContentWidth()
                        ) {
                            Text(text = stringResource(id = R.string.track_preview_race).uppercase())
                        }
                    }
                }
                /**
                 * TODO: place a leaderboard summary here for this track.
                 */
            }
        }
        is TrackPreviewUiState.Loading -> {
            /**
             * TODO: some sort of loading indicator here? Or is it not really necessary?
             */
        }
        is TrackPreviewUiState.Failed -> {
            /**
             * TODO: failed to load the track.
             */
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewTrackPreviewBottomSheet(

) {
    var showBottomSheet by remember { mutableStateOf<Boolean>(false) }
    val track = ExampleData.getExampleTrack()

    HawkSpeedTheme {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text(text = "Show bottom sheet") },
                    icon = { Icon(imageVector = Icons.Default.Menu, contentDescription = "menu") },
                    onClick = {
                        showBottomSheet = true
                    }
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {

            }
            if(showBottomSheet) {
                TrackPreviewModalBottomSheet(
                    trackPreviewUiState = TrackPreviewUiState.TrackPreview(
                        track = track,
                        raceModePromptUiState = RaceModePromptUiState.CantEnterRaceMode,
                        ratingUiState = TrackRatingUiState.GotTrackRating(
                            trackUid = track.trackUid,
                            numPositiveVotes = track.numPositiveVotes,
                            numNegativeVotes = track.numNegativeVotes,
                            yourRating = track.yourRating
                        )
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewTrackPreview(

) {
    val track = ExampleData.getExampleTrack()

    HawkSpeedTheme {
        Scaffold { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                TrackPreview(
                    trackPreviewUi = TrackPreviewUiState.TrackPreview(
                        track = track,
                        raceModePromptUiState = RaceModePromptUiState.CantEnterRaceMode,
                        ratingUiState = TrackRatingUiState.GotTrackRating(
                            trackUid = track.trackUid,
                            numPositiveVotes = track.numPositiveVotes,
                            numNegativeVotes = track.numNegativeVotes,
                            yourRating = track.yourRating
                        )
                    ),
                    modifier = Modifier
                        .padding(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                )
            }
        }
    }
}