package com.vljx.hawkspeed.ui.screens.authenticated.leaderboarddetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.Bronze
import com.vljx.hawkspeed.ui.theme.Gold
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.ui.theme.Silver
import com.vljx.hawkspeed.util.ExampleData

@Composable
fun RaceLeaderboardDetailScreen(
    onViewUserDetailClicked: ((User) -> Unit)? = null,
    onViewTrackDetailClicked: ((Track) -> Unit)? = null,
    onViewVehicleDetailClicked: ((Vehicle) -> Unit)? = null,

    raceLeaderboardDetailViewModel: RaceLeaderboardDetailViewModel = hiltViewModel()
) {
    val raceLeaderboardDetailUiState: RaceLeaderboardDetailUiState by raceLeaderboardDetailViewModel.raceLeaderboardDetailUiState.collectAsState()
    when(raceLeaderboardDetailUiState) {
        is RaceLeaderboardDetailUiState.RaceTrackLeaderboardDetail ->
            RaceLeaderboardDetail(
                raceTrackLeaderboardDetail = raceLeaderboardDetailUiState as RaceLeaderboardDetailUiState.RaceTrackLeaderboardDetail,
                onViewUserDetailClicked = onViewUserDetailClicked
            )

        is RaceLeaderboardDetailUiState.Loading ->
            LoadingScreen()

        is RaceLeaderboardDetailUiState.RaceLeaderboardLoadFailed ->
            RaceLeaderboardDetailFailed(
                leaderboardLoadFailed = raceLeaderboardDetailUiState as RaceLeaderboardDetailUiState.RaceLeaderboardLoadFailed
            )
    }
}

@Composable
fun RaceLeaderboardDetail(
    raceTrackLeaderboardDetail: RaceLeaderboardDetailUiState.RaceTrackLeaderboardDetail,

    onViewUserDetailClicked: ((User) -> Unit)? = null,
    onViewTrackDetailClicked: ((Track) -> Unit)? = null,
    onViewVehicleDetailClicked: ((Vehicle) -> Unit)? = null
) {
    // Set up a scaffold. For all the content.
    Scaffold(
        modifier = Modifier
    ) { paddingValues ->
        // Setup a new column to take from the scaffold padding.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            // Setup the leaderboard head.
            LeaderboardHead(
                raceLeaderboard = raceTrackLeaderboardDetail.raceLeaderboard
            )
            Spacer(modifier = Modifier.height(48.dp))
            // Now the body, which contains info about the track and time of completion.
            LeaderboardTrackRaceInfo(
                track = raceTrackLeaderboardDetail.track,
                trackPath = raceTrackLeaderboardDetail.trackPath,
                raceLeaderboard = raceTrackLeaderboardDetail.raceLeaderboard,

                onViewUserDetailClicked = onViewUserDetailClicked,
                onViewTrackDetailClicked = onViewTrackDetailClicked,
                onViewVehicleDetailClicked = onViewVehicleDetailClicked
            )
            // Another spacer before the time taken.
            Spacer(modifier = Modifier.height(64.dp))
            LeaderboardRaceInfo(
                raceLeaderboard = raceTrackLeaderboardDetail.raceLeaderboard
            )
        }
    }
}

@Composable
fun LeaderboardHead(
    raceLeaderboard: RaceLeaderboard
) {
    // Set up a new row to contain the center aligned content.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 48.dp)
            .wrapContentWidth()
    ) {
        when(raceLeaderboard.finishingPlace) {
            1, 2, 3 -> {
                // If we have first, second or third place, we will also display a badge.
                // First, get the colour of the medal, and the text for the placement.
                val medalColour: Color
                val placementText: String
                when(raceLeaderboard.finishingPlace) {
                    1 -> {
                        medalColour = Gold
                        placementText = stringResource(id = R.string.race_leaderboard_first)
                    }
                    2 -> {
                        medalColour = Silver
                        placementText = stringResource(id = R.string.race_leaderboard_second)
                    }
                    3 -> {
                        medalColour = Bronze
                        placementText = stringResource(id = R.string.race_leaderboard_third)
                    }
                    else -> throw NotImplementedError() // This should never be triggered.
                }
                // Create a column, 96 dp in size for the medal.
                Column(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(96.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Draw the medal here.
                    Image(
                        modifier = Modifier
                            .fillMaxSize(),
                        painter = painterResource(id = R.drawable.ic_medal),
                        colorFilter = ColorFilter.tint(medalColour),
                        contentDescription = "medal"
                    )
                }
                // Now, another column to contain the textual placement.
                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = 4.dp)
                ) {
                    // Set this to the placement's text.
                    Text(
                        text = placementText,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold
                    )
                    // Static text 'place'
                    Text(
                        text = stringResource(id = R.string.race_leaderboard_place).lowercase(),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            else -> {
                // Otherwise, a generic figurehead that does not indicate any special visual effects.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.race_leaderboard_place_hash, raceLeaderboard.finishingPlace),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 7.9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardTrackRaceInfo(
    track: Track,
    trackPath: TrackPath,
    raceLeaderboard: RaceLeaderboard,

    onViewUserDetailClicked: ((User) -> Unit)? = null,
    onViewTrackDetailClicked: ((Track) -> Unit)? = null,
    onViewVehicleDetailClicked: ((Vehicle) -> Unit)? = null
) {
    // Set up a new row to contain the center aligned content.
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.race_leaderboard_awarded_to),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(6.dp))
            // TODO: call a composable for a small card type UI element that informs the user's name etc.
            Text(
                modifier = Modifier
                    .clickable {
                        onViewUserDetailClicked?.invoke(raceLeaderboard.player)
                    },
                text = raceLeaderboard.player.userName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(id = R.string.race_leaderboard_for_attempt),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(6.dp))
            // TODO: call a composable for a small card type UI element that informs the track's name, type etc.
            Text(
                modifier = Modifier
                    .clickable {
                        onViewTrackDetailClicked?.invoke(track)
                    },
                text = track.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(id = R.string.race_leaderboard_completed_at),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = raceLeaderboard.dateTimeAwarded,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(id = R.string.race_leaderboard_in_their),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(6.dp))
            // TODO: call a composable for a small card type UI element that informs the vehicle's title etc.
            Text(
                modifier = Modifier
                    .clickable {
                        onViewVehicleDetailClicked?.invoke(raceLeaderboard.vehicle)
                    },
                text = raceLeaderboard.vehicle.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LeaderboardRaceInfo(
    raceLeaderboard: RaceLeaderboard
) {
    // Set up a new row to contain the center aligned content.
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Time taken",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleLarge
            )
            // Create a huge text for the race time.
            Text(
                text = raceLeaderboard.raceTime,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            // TODO: create a grid layout for the other interesting aspects of the race including; % of track missed, average speed.
        }
    }
}

@Composable
fun RaceLeaderboardDetailFailed(
    leaderboardLoadFailed: RaceLeaderboardDetailUiState.RaceLeaderboardLoadFailed
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {

        }
    }
}

@Preview
@Composable
fun PreviewRaceLeaderboardDetail(

) {
    HawkSpeedTheme {
        RaceLeaderboardDetail(
            raceTrackLeaderboardDetail = RaceLeaderboardDetailUiState.RaceTrackLeaderboardDetail(
                track = ExampleData.getExampleTrack(),
                trackPath = ExampleData.getExampleTrackPath(),
                raceLeaderboard = ExampleData.getExampleLeaderboard()[1]
            )
        )
    }
}

@Preview
@Composable
fun PreviewLeaderboardDetailFailed(

) {
    HawkSpeedTheme {
        RaceLeaderboardDetailFailed(
            leaderboardLoadFailed = RaceLeaderboardDetailUiState.RaceLeaderboardLoadFailed(
                ResourceError.GeneralError("unknown", null)
            )
        )
    }
}