package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import kotlinx.coroutines.flow.Flow

interface RaceLeaderboardRepository {
    /**
     * Open and return a flow for a leaderboard entry, for a specific race UID. The intended use for this is that initially, there will be nothing to emit for an
     * ongoing race. But as soon as that race completes successfully, and the leaderboard entry itself is cached, this flow will then return a value; also indicating
     * the race has been successfully completed.
     */
    fun getLeaderboardEntryForRace(requestGetRace: RequestGetRace): Flow<RaceLeaderboard?>
}