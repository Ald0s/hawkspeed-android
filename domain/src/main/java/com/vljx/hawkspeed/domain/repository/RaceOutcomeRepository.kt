package com.vljx.hawkspeed.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.models.race.RaceOutcome
import com.vljx.hawkspeed.domain.requests.track.PageLeaderboardRequest
import kotlinx.coroutines.flow.Flow

interface RaceOutcomeRepository {
    @ExperimentalPagingApi
    fun pageLeaderboardForTrack(pageLeaderboardRequest: PageLeaderboardRequest): Flow<PagingData<RaceOutcome>>
}