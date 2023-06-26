package com.vljx.hawkspeed.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.models.race.RaceOutcome
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    /**
     * Page the given track's leaderboard.
     */
    @ExperimentalPagingApi
    fun pageLeaderboardForTrack(requestPageTrackLeaderboard: RequestPageTrackLeaderboard): Flow<PagingData<RaceOutcome>>
}