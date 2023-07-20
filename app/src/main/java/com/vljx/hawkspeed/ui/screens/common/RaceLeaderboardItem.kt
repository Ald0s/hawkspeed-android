package com.vljx.hawkspeed.ui.screens.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.ui.theme.Bronze
import com.vljx.hawkspeed.ui.theme.Gold
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.ui.theme.Silver
import com.vljx.hawkspeed.util.ExampleData

@Composable
fun RaceLeaderboardHeaderItem(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = Modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 32.dp,
                    bottom = 16.dp
                )
                .fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .height(32.dp)
                    .weight(0.2f)
            ) {
                Text(
                    text = stringResource(id = R.string.race_leaderboard_place).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.5f)
            ) {
                Text(
                    text = stringResource(id = R.string.race_leaderboard_username).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.4f)
            ) {
                Text(
                    text = stringResource(id = R.string.race_leaderboard_time).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun RaceLeaderboardItem(
    raceLeaderboard: RaceLeaderboard,

    onViewAttemptClicked: ((RaceLeaderboard) -> Unit)? = null
) {
    val color: Color = when(raceLeaderboard.finishingPlace) {
        1 -> Gold
        2 -> Silver
        3 -> Bronze
        else -> Color.White
    }
    val paddingTopBottom: Dp = 12.dp
    val isOnRankingBoard: Boolean = when(raceLeaderboard.finishingPlace) {
        1, 2, 3 -> true
        else -> false
    }

    Surface(
        tonalElevation = 5.dp,
        modifier = Modifier
            .clickable {
                // Whenever surface is clicked, view the leaderboard item's detail.
                onViewAttemptClicked?.invoke(raceLeaderboard)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = paddingTopBottom,
                    bottom = paddingTopBottom
                )
                .fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .height(32.dp)
                    .weight(0.2f)
            ) {
                if(isOnRankingBoard) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_medal),
                        colorFilter = ColorFilter.tint(color),
                        contentDescription = "medal"
                    )
                } else {
                    Text(
                        text = "#${raceLeaderboard.finishingPlace}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.5f)
            ) {
                Text(
                    text = raceLeaderboard.player.userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 18.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.3f)
            ) {
                Text(
                    text = raceLeaderboard.raceTime,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 18.sp
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
fun PreviewRaceLeaderboardHeaderItem(

) {
    HawkSpeedTheme {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            RaceLeaderboardHeaderItem()
        }
    }
}

@Preview
@Composable
fun PreviewRaceLeaderboardItem(

) {
    HawkSpeedTheme {
        Surface {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                ExampleData.getExampleLeaderboard().forEach {
                    RaceLeaderboardItem(
                        raceLeaderboard = it
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}