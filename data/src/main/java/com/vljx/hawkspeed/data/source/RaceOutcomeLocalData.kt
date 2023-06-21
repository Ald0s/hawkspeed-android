package com.vljx.hawkspeed.data.source

import androidx.paging.PagingSource
import com.vljx.hawkspeed.data.database.entity.RaceOutcomeEntity
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageLeaderboard

interface RaceOutcomeLocalData {
    fun pageRaceOutcomesFromTrack(requestPageLeaderboard: RequestPageLeaderboard): PagingSource<Int, RaceOutcomeEntity>
    suspend fun upsertRaceLeaderboard(raceLeaderboardPageModel: RaceLeaderboardPageModel)
    suspend fun clearLeaderboardFor(trackUid: String)
}