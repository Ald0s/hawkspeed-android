package com.vljx.hawkspeed.ui.dialogs.trackpreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.ui.screens.authenticated.trackdetail.TrackRatingUiState
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData

@Composable
fun TrackPreviewDialog(
    trackUid: String,
    onRaceModeClicked: (Track) -> Unit,
    onViewTrackDetailClicked: (Track) -> Unit,
    onViewTrackCommentsClicked: (Track, Boolean) -> Unit,
    onViewTrackLeaderboardClicked: (Track) -> Unit,
    onDismiss: () -> Unit,
    trackPreviewViewModel: TrackPreviewViewModel = hiltViewModel()
) {
    // Call the dialog composable to open our preview UI. This should open with the loading composable.
    Dialog(
        onDismissRequest = onDismiss
    ) {
        // Collect the track preview UI state and the race prompt UI state here.
        val trackPreviewUi by trackPreviewViewModel.trackPreviewUiState.collectAsState()
        // Call the actual card composable now with the collected state and all callbacks.
        TrackPreviewCard(
            trackPreviewUi = trackPreviewUi,
            onRaceModeClicked = onRaceModeClicked,
            onViewTrackDetailClicked = onViewTrackDetailClicked,
            onViewTrackLeaderboardClicked = onViewTrackLeaderboardClicked,
            onPostComment = { track ->
                onViewTrackCommentsClicked(track, true)
            },
            onViewComments = { track ->
                onViewTrackCommentsClicked(track, false)
            }
        )
    }
    // In a launched side effect, immediately set the selected track UID in view model. We must do this in a side effect otherwise this will
    // be called every recomposition.
    LaunchedEffect(key1 = Unit, block = {
        trackPreviewViewModel.selectTrack(trackUid)
    })
}

@Composable
fun TrackPreviewCard(
    trackPreviewUi: TrackPreviewUiState,
    onRaceModeClicked: ((Track) -> Unit)? = null,
    onViewTrackDetailClicked: ((Track) -> Unit)? = null,
    onViewTrackLeaderboardClicked: ((Track) -> Unit)? = null,
    onPostComment: ((Track) -> Unit)? = null,
    onViewComments: ((Track) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .requiredHeight(LocalConfiguration.current.screenHeightDp.dp * 0.50f)
            .requiredWidth(LocalConfiguration.current.screenWidthDp.dp * 0.85f),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        when(trackPreviewUi) {
            is TrackPreviewUiState.TrackPreview -> {
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
                // Get the actual track.
                val track = trackPreviewUi.track

                Column(
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    // Include the track heading.
                    TrackHeading(
                        track = track,
                        raceModePromptUiState = trackPreviewUi.raceModePromptUiState,
                        onRaceModeClicked = onRaceModeClicked
                    )
                    // Now include the track body.
                    TrackBody(
                        onViewTrackDetailClicked = onViewTrackDetailClicked,
                        onViewTrackLeaderboardClicked = onViewTrackLeaderboardClicked,
                        track = track
                    )
                    // TODO: now include the reviews section.
                }
            }
            is TrackPreviewUiState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            }
            is TrackPreviewUiState.Failed -> {
                // TODO: proper failed status page for the track preview.
                throw NotImplementedError()
            }
        }
    }
}

@Composable
fun TrackHeading(
    track: Track,
    raceModePromptUiState: RaceModePromptUiState,
    onRaceModeClicked: ((Track) -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = track.name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            )
            Button(
                onClick = {
                    onRaceModeClicked?.let { it(track) }
                },
                enabled = raceModePromptUiState is RaceModePromptUiState.CanEnterRaceMode,
                shape = RectangleShape,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.track_preview_race).uppercase())
            }
        }
    }
}

@Composable
fun TrackBody(
    track: Track,
    onViewTrackDetailClicked: ((Track) -> Unit)? = null,
    onViewTrackLeaderboardClicked: ((Track) -> Unit)? = null
) {
    Row(
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Display the track preview.
            TrackAboutPreview(
                track = track,
                onViewTrackDetailClicked = onViewTrackDetailClicked
            )
            Spacer(
                modifier = Modifier
                    .height(24.dp)
            )
            // Display the track leaderboard.
            TrackLeaderboardPreview(
                topLeaderboard = track.topLeaderboard,
                onViewLeaderboardClicked = {
                    onViewTrackLeaderboardClicked?.let { it(track) }
                }
            )
        }
    }
}

@Composable
fun TrackAboutPreview(
    track: Track,
    modifier: Modifier = Modifier,
    onViewTrackDetailClicked: ((Track) -> Unit)? = null
) {
    // The outer row, containing the entire UI element. This will take padding cues from caller.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // First column. This will contain the about title and the about description.
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            // The title row.
            Row {
                Text(
                    text = stringResource(id = R.string.track_about),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )
            }
            // The description row.
            Row {
                Text(
                    text = track.description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                )
            }
        }
        // The second column. This is the button to open up the track's detail.
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    onViewTrackDetailClicked?.let { it(track) }
                }
            ) {
                Icon(
                    Icons.Filled.KeyboardArrowRight,
                    "right"
                )
            }
        }
    }
}

@Composable
fun TrackLeaderboardPreview(
    topLeaderboard: List<RaceLeaderboard>,
    modifier: Modifier = Modifier,
    onViewLeaderboardClicked: (() -> Unit)? = null
) {
    if(topLeaderboard.isNotEmpty()) {
        // Leaderboard not empty. Create an outer row, which will adhere to padding from caller.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            // Now, a column for the title and actual leaderboard.
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                // A row for the title.
                Row {
                    Text(
                        text = stringResource(id = R.string.track_leaderboard),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                }
                // A column for the leaderboard.
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Center
                ) {
                    // A row for the table header for the leaderboard.
                    Row {
                        // Three columns; one for place, player name and time.
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
                    // Now, iterate all leaderboard entries, and create a row for each, with columns matching headers.
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
            }
            // Next, a column for the icon button.
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        onViewLeaderboardClicked?.invoke()
                    }
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        "right"
                    )
                }
            }
        }
    } else {
        // Leaderboard is empty, we'll simply display a message indicating that.
        Row {
            Text(
                text = stringResource(id = R.string.track_leaderboard),
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
        }
        Row(
            modifier = modifier
        ) {
            Text(
                text = stringResource(id = R.string.track_leaderboard_empty)
            )
        }
    }
}

@Composable
fun TrackReviews(
    track: Track,
    trackRatingUiState: TrackRatingUiState,
    modifier: Modifier = Modifier,
    onPostComment: ((Track) -> Unit)? = null,
    onViewComments: ((Track) -> Unit)? = null,
    onUpvoteClicked: (() -> Unit)? = null,
    onDownvoteClicked: (() -> Unit)? = null
) {
    /**
     * TODO: remember, when any of the upvote/downvote buttons are tapped, they should be set to a disabled state, then we will rely on an emission of a new UI state to enable them.
     * TODO: a key attribute required by this check should be whether the player has completed the track at least once, implement this.
     */
}

@Preview(showBackground = true)
@Composable
fun PreviewTrackPreviewCard() {
    HawkSpeedTheme {
        Dialog(onDismissRequest = { /*TODO*/ }) {
            TrackPreviewCard(
                trackPreviewUi = TrackPreviewUiState.TrackPreview(
                    track = ExampleData.getExampleTrack(
                        trackUid = "YARRABOULEVARD",
                        description = "One of the better tracks in the Melbourne area. A few hairpins and other sharp turns."
                    ),
                    raceModePromptUiState = RaceModePromptUiState.CantEnterRaceMode,
                    ratingUiState = TrackRatingUiState.GotTrackRating(
                        trackUid = "YARRABOULEVARD",
                        numPositiveVotes = 0,
                        numNegativeVotes = 0,
                        yourRating = null
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTrackReviews(
    
) {
    HawkSpeedTheme {
        TrackReviews(
            track = ExampleData.getExampleTrack(
                trackUid = "TRACK01",
                description = "One of the better tracks in the Melbourne area. A few hairpins and other sharp turns."
            ),
            trackRatingUiState = TrackRatingUiState.GotTrackRating(
                "TRACK01",
                6,
                4,
                null
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTrackAbout(

) {
    HawkSpeedTheme {
        TrackAboutPreview(
            track = ExampleData.getExampleTrack(
                trackUid = "YARRABOULEVARD",
                description = "One of the better tracks in the Melbourne area. A few hairpins and other sharp turns."
            ),
            modifier = Modifier
                .padding(16.dp)
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
                RaceLeaderboard("RACE01", finishingPlace = 1, 26450, 100, 26450, User("USER01", "aldos", 0, false, true), Vehicle("VEHICLE01", "1994 Toyota Supra", true), "TRACK01"),
                RaceLeaderboard("RACE01", finishingPlace = 2, 54210, 100, 54210, User("USER02", "user1", 0, false, false), Vehicle("VEHICLE02", "1994 Toyota Supra", false),"TRACK01"),
                RaceLeaderboard("RACE01", finishingPlace = 3, 125134, 100, 125134, User("USER03", "user2", 0, false, false), Vehicle("VEHICLE03", "1994 Toyota Supra", false),"TRACK01")
            ),
            modifier = Modifier
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTrackBody(

) {
    HawkSpeedTheme {
        TrackBody(
            track = ExampleData.getExampleTrack(
                trackUid = "YARRABOULEVARD",
                description = "One of the better tracks in the Melbourne area. A few hairpins and other sharp turns."
            )
        )
    }
}