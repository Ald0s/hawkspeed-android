package com.vljx.hawkspeed.data.source.race

import androidx.paging.PagingSource
import com.vljx.hawkspeed.data.database.entity.race.RaceLeaderboardEntity
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardModel
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard
import kotlinx.coroutines.flow.Flow

interface RaceLeaderboardLocalData {
    /**
     * Return a paging source for the leaderboard for a specific track currently cached.
     */
    fun pageRaceLeaderboardFromTrack(requestPageTrackLeaderboard: RequestPageTrackLeaderboard): PagingSource<Int, RaceLeaderboardEntity>

    /**
     * Open a flow for selecting and returning a race leaderboard entry for the given race.
     */
    fun selectLeaderboardEntryForRace(requestGetRace: RequestGetRace): Flow<RaceLeaderboardModel?>

    /**
     * Upsert the given page of leaderboard entries.
     */
    suspend fun upsertRaceLeaderboard(raceLeaderboardPageModel: RaceLeaderboardPageModel)

    /**
     * Clear the entire cached leaderboard for the given track.
     */
    suspend fun clearLeaderboardFor(trackUid: String)
}