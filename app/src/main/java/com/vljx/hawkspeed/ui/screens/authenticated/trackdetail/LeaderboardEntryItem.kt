package com.vljx.hawkspeed.ui.screens.authenticated.trackdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData

@Composable
fun LeaderboardEntryItem(
    raceLeaderboard: RaceLeaderboard,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .weight(0.1f)
        ) {
            Text(text = "#${raceLeaderboard.finishingPlace}")
        }
        Column(
            modifier = Modifier
                .weight(0.3f)
        ) {
            Text(text = raceLeaderboard.player.userName)
        }
        Column(
            modifier = Modifier
                .weight(0.4f)
        ) {
            Text(text = raceLeaderboard.vehicle.title)
        }
        Column(
            modifier = Modifier
                .weight(0.2f)
        ) {
            Text(text = raceLeaderboard.prettyTime)
        }
    }
}

@Composable
fun LeaderboardEntryMinItem(
    raceLeaderboard: RaceLeaderboard,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .weight(0.2f)
        ) {
            Text(text = "#${raceLeaderboard.finishingPlace}")
        }
        Column(
            modifier = Modifier
                .weight(0.4f)
        ) {
            Text(text = raceLeaderboard.player.userName)
        }
        Column(
            modifier = Modifier
                .weight(0.4f)
        ) {
            Text(text = raceLeaderboard.prettyTime)
        }
    }
}

@Preview
@Composable
fun PreviewLeaderboardEntryItem(

) {
    val leaderboard = ExampleData.getExampleLeaderboard()

    HawkSpeedTheme {
        Column(
            modifier = Modifier
        ) {
            leaderboard.forEach { leader ->
                LeaderboardEntryItem(raceLeaderboard = leader)
            }
        }
    }
}

@Preview
@Composable
fun PreviewLeaderboardEntryMinItem(

) {
    val leaderboard = ExampleData.getExampleLeaderboard()

    HawkSpeedTheme {
        Column(
            modifier = Modifier
        ) {
            leaderboard.forEach { leader ->
                LeaderboardEntryMinItem(raceLeaderboard = leader)
            }
        }
    }
}