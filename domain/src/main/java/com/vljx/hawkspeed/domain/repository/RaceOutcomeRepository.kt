package com.vljx.hawkspeed.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.models.race.RaceOutcome
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageLeaderboard
import kotlinx.coroutines.flow.Flow

interface RaceOutcomeRepository {
    /**
     * Page the given track's leaderboard.
     */
    @ExperimentalPagingApi
    fun pageLeaderboardForTrack(requestPageLeaderboard: RequestPageLeaderboard): Flow<PagingData<RaceOutcome>>
}