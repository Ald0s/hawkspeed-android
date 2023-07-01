package com.vljx.hawkspeed.data.database

import androidx.paging.PagingSource
import com.vljx.hawkspeed.data.database.dao.RaceLeaderboardDao
import com.vljx.hawkspeed.data.database.entity.race.RaceLeaderboardEntity
import com.vljx.hawkspeed.data.database.mapper.RaceLeaderboardEntityMapper
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.data.source.race.RaceLeaderboardLocalData
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard
import javax.inject.Inject

class RaceLeaderboardLocalDataImpl @Inject constructor(
    private val raceLeaderboardDao: RaceLeaderboardDao,

    private val raceLeaderboardEntityMapper: RaceLeaderboardEntityMapper
): RaceLeaderboardLocalData {
    override fun pageRaceLeaderboardFromTrack(requestPageTrackLeaderboard: RequestPageTrackLeaderboard): PagingSource<Int, RaceLeaderboardEntity> =
        raceLeaderboardDao.pageRaceLeaderboardFromTrack(requestPageTrackLeaderboard.trackUid)

    override suspend fun upsertRaceLeaderboard(raceLeaderboardPageModel: RaceLeaderboardPageModel) {
        // Map all race outcomes to their entity equivalents.
        val raceOutcomeEntities: List<RaceLeaderboardEntity> = raceLeaderboardPageModel.raceOutcomes.map {
            raceLeaderboardEntityMapper.mapToEntity(it)
        }
        // Now, upsert all of these.
        raceLeaderboardDao.upsert(raceOutcomeEntities)
    }

    override suspend fun clearLeaderboardFor(trackUid: String) =
        raceLeaderboardDao.clearLeaderboardFor(trackUid)
}