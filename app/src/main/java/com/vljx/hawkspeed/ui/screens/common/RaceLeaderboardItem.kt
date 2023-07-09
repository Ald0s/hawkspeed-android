package com.vljx.hawkspeed.ui.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData

@Composable
fun RaceLeaderboardItem(
    raceLeaderboard: RaceLeaderboard,

    onViewAttemptClicked: ((RaceLeaderboard) -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .clickable {
                // Whenever surface is clicked, view the leaderboard item's detail.
                onViewAttemptClicked?.invoke(raceLeaderboard)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .weight(0.2f)
            ) {
                Text(
                    text = "#${raceLeaderboard.finishingPlace}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.4f)
            ) {
                Text(
                    text = raceLeaderboard.player.userName,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.4f)
            ) {
                Text(
                    text = raceLeaderboard.prettyTime,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.1f)
            ) {
                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "more")
            }
        }
    }
}

@Preview
@Composable
fun PreviewRaceLeaderboardItem(

) {
    HawkSpeedTheme {
        RaceLeaderboardItem(
            raceLeaderboard = ExampleData.getExampleLeaderboard().first()
        )
    }
}