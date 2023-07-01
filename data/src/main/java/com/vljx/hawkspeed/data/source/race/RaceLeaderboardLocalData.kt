package com.vljx.hawkspeed.data.source.race

import androidx.paging.PagingSource
import com.vljx.hawkspeed.data.database.entity.race.RaceLeaderboardEntity
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard

interface RaceLeaderboardLocalData {
    /**
     *
     */
    fun pageRaceLeaderboardFromTrack(requestPageTrackLeaderboard: RequestPageTrackLeaderboard): PagingSource<Int, RaceLeaderboardEntity>

    /**
     *
     */
    suspend fun upsertRaceLeaderboard(raceLeaderboardPageModel: RaceLeaderboardPageModel)

    /**
     *
     */
    suspend fun clearLeaderboardFor(trackUid: String)
}