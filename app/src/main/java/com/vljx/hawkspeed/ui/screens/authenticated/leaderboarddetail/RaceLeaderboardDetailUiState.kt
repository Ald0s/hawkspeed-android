package com.vljx.hawkspeed.ui.screens.authenticated.leaderboarddetail

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard

sealed class RaceLeaderboardDetailUiState {
    /**
     * The success state - we have a leaderboard model.
     */
    data class GotRaceLeaderboardDetail(
        val raceLeaderboard: RaceLeaderboard
    ): RaceLeaderboardDetailUiState()

    /**
     * The loading/initial state.
     */
    object Loading: RaceLeaderboardDetailUiState()

    /**
     * The failure state for loading the leaderboard entry.
     */
    data class RaceLeaderboardLoadFailed(
        val resourceError: ResourceError
    ): RaceLeaderboardDetailUiState()
}