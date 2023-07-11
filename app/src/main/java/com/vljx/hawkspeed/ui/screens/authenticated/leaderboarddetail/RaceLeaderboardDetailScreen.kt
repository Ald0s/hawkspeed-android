package com.vljx.hawkspeed.ui.screens.authenticated.leaderboarddetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData

@Composable
fun RaceLeaderboardDetailScreen(
    onViewUserDetailClicked: ((User) -> Unit)? = null,
    raceLeaderboardDetailViewModel: RaceLeaderboardDetailViewModel = hiltViewModel()
) {
    val raceLeaderboardDetailUiState: RaceLeaderboardDetailUiState by raceLeaderboardDetailViewModel.raceLeaderboardDetailUiState.collectAsState()
    when(raceLeaderboardDetailUiState) {
        is RaceLeaderboardDetailUiState.GotRaceLeaderboardDetail ->
            RaceLeaderboardDetail(
                onViewUserDetailClicked = onViewUserDetailClicked,
                gotLeaderboardDetail = raceLeaderboardDetailUiState as RaceLeaderboardDetailUiState.GotRaceLeaderboardDetail
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
    onViewUserDetailClicked: ((User) -> Unit)? = null,
    gotLeaderboardDetail: RaceLeaderboardDetailUiState.GotRaceLeaderboardDetail
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {

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
            gotLeaderboardDetail = RaceLeaderboardDetailUiState.GotRaceLeaderboardDetail(
                raceLeaderboard = ExampleData.getExampleLeaderboard().first()
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