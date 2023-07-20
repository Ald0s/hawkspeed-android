package com.vljx.hawkspeed.ui.screens.authenticated.leaderboarddetail

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath

sealed class RaceLeaderboardDetailUiState {
    /**
     * The success state - we have a leaderboard model.
     */
    data class RaceTrackLeaderboardDetail(
        val track: Track,
        val trackPath: TrackPath,
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