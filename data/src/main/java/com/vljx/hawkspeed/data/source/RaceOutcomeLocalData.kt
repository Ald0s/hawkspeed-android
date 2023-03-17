package com.vljx.hawkspeed.data.source

import androidx.paging.PagingSource
import com.vljx.hawkspeed.data.database.entity.RaceOutcomeEntity
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.domain.requests.track.PageLeaderboardRequest

interface RaceOutcomeLocalData {
    fun pageRaceOutcomesFromTrack(pageLeaderboardRequest: PageLeaderboardRequest): PagingSource<Int, RaceOutcomeEntity>
    suspend fun upsertRaceLeaderboard(raceLeaderboardPageModel: RaceLeaderboardPageModel)
    suspend fun clearLeaderboardFor(trackUid: String)
}