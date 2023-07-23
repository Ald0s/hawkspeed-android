package com.vljx.hawkspeed.domain.repository

import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.requestmodels.user.RequestPageRaceHistory
import kotlinx.coroutines.flow.Flow

// TODO: finish this.
interface UserRaceHistoryRepository {
    /**
     * Page race history for the given User and optional filters.
     */
    fun pageRaceHistory(requestPageRaceHistory: RequestPageRaceHistory): Flow<PagingData<RaceLeaderboard>>
}