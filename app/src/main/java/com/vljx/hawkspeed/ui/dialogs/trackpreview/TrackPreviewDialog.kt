package com.vljx.hawkspeed.ui.dialogs.trackpreview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.race.RaceOutcome
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPoint
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.ui.screens.authenticated.trackdetail.TrackRatingUiState
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TrackPreviewDialog(
    trackUid: String,
    onViewTrackDetailClicked: (Track) -> Unit,
    onViewTrackCommentsClicked: (Track, Boolean) -> Unit,
    onViewTrackLeaderboardClicked: (Track) -> Unit,
    onDismiss: () -> Unit,
    trackPreviewViewModel: TrackPreviewViewModel = hiltViewModel()
) {
    // Immediately select the given track in our view model.
    trackPreviewViewModel.selectTrack(trackUid)

    // Collect the track preview here.
    val trackPreviewUiState by trackPreviewViewModel.trackPreviewUiState.collectAsState()

    when(trackPreviewUiState) {
        is TrackPreviewUiState.GotTrack -> {
            val track = (trackPreviewUiState as TrackPreviewUiState.GotTrack).track
            // Collect as state the race prompt here.
            val racePromptUiState by trackPreviewViewModel.racePromptUiState.collectAsState()

            Dialog(
                onDismissRequest = onDismiss
            ) {
                TrackPreviewCard(
                    track = track,
                    onViewTrackDetailClicked = onViewTrackDetailClicked,
                    onViewTrackLeaderboardClicked = onViewTrackLeaderboardClicked,
                    onPostComment = { track ->
                        onViewTrackCommentsClicked(track, true)
                    },
                    onViewComments = { track ->
                        onViewTrackCommentsClicked(track, false)
                    },
                    racePromptUiState = racePromptUiState
                )
            }
        }

        is TrackPreviewUiState.Loading -> {
            // TODO: some loading indication.
        }

        is TrackPreviewUiState.Failed -> {
            // TODO: some failed indication.
        }
    }
}

@Composable
fun TrackPreviewCard(
    track: Track,
    onViewTrackDetailClicked: (Track) -> Unit,
    onViewTrackLeaderboardClicked: (Track) -> Unit,
    onPostComment: (Track) -> Unit,
    onViewComments: (Track) -> Unit,
    racePromptUiState: RacePromptUiState
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        /**
         * TODO: On the track preview card, we want to display the track's name, description
         * TODO: Display ratings, w/ up + down vote buttons.
         * TODO: Display a Race button that will enable itself when the User is positioned such that they can start a new race.
         * TODO: We also want to display perhaps current top 3 on leaderboard.
         * TODO: Display a Comment button along with the first page of comments for the track.
         * For vote buttons, when tapped, they will go from outline to filled in. Tap filled in again to remove a vote, or tap the other side to change the vote.
         * For top 3 on leaderboard, we'll need to modify server & cache such that the top 3 race outcomes are sent with each track and cached that way. Tapping on any of the three will go to the track's detail, and show leaderboard.
         * For the most recent 10 comments, we must write a function that will not page but specifically fetch the first page of comments. Tapping on the Comment button or any of the comments should take user to track details, then to all comments.
         */

        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Include the track heading.
            TrackHeading(track = track)
            // Now include the track body.
            TrackBody(track = track)
        }
    }
}

@Composable
fun TrackHeading(
    track: Track
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = track.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            )
            Button(
                onClick = {

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.track_preview_race))
            }
        }
    }
}

@Composable
fun TrackBody(
    track: Track
) {
    Row {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.track_about),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                )
                IconButton(
                    onClick = { /*TODO*/ }
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        "right"
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .height(24.dp)
            )
            Text(
                text = stringResource(id = R.string.track_leaderboard),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )
            TrackLeaderboardPreview(
                topLeaderboard = track.topLeaderboard,
                onViewLeaderboardClicked = {}
            )
        }
    }
}

@Composable
fun TrackLeaderboardPreview(
    topLeaderboard: List<RaceOutcome>,
    onViewLeaderboardClicked: () -> Unit
) {
    if(topLeaderboard.isEmpty()){
        Text(
            text = stringResource(id = R.string.track_leaderboard_empty)
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row {
                        Column(
                            modifier = Modifier
                                .weight(0.3f)
                                .padding(2.dp)
                        ) {
                            Text(
                                text = "Place",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(2.dp)
                        ) {
                            Text(
                                text = "Player",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(2.dp)
                        ) {
                            Text(
                                text = "Time",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    topLeaderboard.forEach { raceOutcome ->
                        Row {
                            Column(
                                modifier = Modifier
                                    .weight(0.3f)
                                    .padding(2.dp)
                            ) {
                                Text(text = "#${raceOutcome.finishingPlace}")
                            }

                            Column(
                                modifier = Modifier
                                    .weight(0.4f)
                                    .padding(2.dp)
                            ) {
                                Text(
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    text = raceOutcome.player.userName
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(0.4f)
                                    .padding(2.dp)
                            ) {
                                Text(text = raceOutcome.prettyTime)
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onViewLeaderboardClicked
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        "right"
                    )
                }
            }
        }
    }
}

@Composable
fun TrackRatings(
    trackRatingUiState: TrackRatingUiState,
    onUpvoteClicked: () -> Unit,
    onDownvoteClicked: () -> Unit
) {

}

@Preview(showBackground = true)
@Composable
fun PreviewTrackPreviewCard() {
    HawkSpeedTheme {
        Dialog(onDismissRequest = { /*TODO*/ }) {
            TrackPreviewCard(
                track = ExampleData.getExampleTrack(
                    description = "One of the better tracks in the Melbourne area. A few hairpins and other sharp turns."
                ),
                onViewTrackDetailClicked = { track -> },
                onViewTrackLeaderboardClicked = { track -> },
                onViewComments = { track -> },
                onPostComment = { track -> },
                racePromptUiState = RacePromptUiState.CantRace
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTrackRatings(
    
) {
    HawkSpeedTheme {
        TrackRatings(
            trackRatingUiState = TrackRatingUiState.GotTrackRating(
                "TRACK01",
                6,
                4,
                null
            ),
            onUpvoteClicked = { },
            onDownvoteClicked = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTrackLeaderboardPreview(

) {
    HawkSpeedTheme {
        TrackLeaderboardPreview(
            topLeaderboard = listOf(
                RaceOutcome("RACE01", finishingPlace = 1, 26450, 100, 26450, User("USER01", "aldos", 0, false, true), "TRACK01"),
                RaceOutcome("RACE01", finishingPlace = 2, 54210, 100, 54210, User("USER02", "user1", 0, false, false), "TRACK01"),
                RaceOutcome("RACE01", finishingPlace = 3, 125134, 100, 125134, User("USER03", "user2", 0, false, false), "TRACK01")
            ),
            onViewLeaderboardClicked = { }
        )
    }
}