package com.vljx.hawkspeed.data.database

import androidx.paging.PagingSource
import com.vljx.hawkspeed.data.database.dao.RaceOutcomeDao
import com.vljx.hawkspeed.data.database.entity.RaceOutcomeEntity
import com.vljx.hawkspeed.data.database.mapper.RaceOutcomeEntityMapper
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.data.source.race.RaceOutcomeLocalData
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard
import javax.inject.Inject

class RaceOutcomeLocalDataImpl @Inject constructor(
    private val raceOutcomeDao: RaceOutcomeDao,

    private val raceOutcomeEntityMapper: RaceOutcomeEntityMapper
): RaceOutcomeLocalData {
    override fun pageRaceOutcomesFromTrack(requestPageTrackLeaderboard: RequestPageTrackLeaderboard): PagingSource<Int, RaceOutcomeEntity> =
        raceOutcomeDao.pageRaceOutcomesFromTrack(requestPageTrackLeaderboard.trackUid)

    override suspend fun upsertRaceLeaderboard(raceLeaderboardPageModel: RaceLeaderboardPageModel) {
        // Map all race outcomes to their entity equivalents.
        val raceOutcomeEntities: List<RaceOutcomeEntity> = raceLeaderboardPageModel.raceOutcomes.map {
            raceOutcomeEntityMapper.mapToEntity(it)
        }
        // Now, upsert all of these.
        raceOutcomeDao.upsert(raceOutcomeEntities)
    }

    override suspend fun clearLeaderboardFor(trackUid: String) =
        raceOutcomeDao.clearRaceOutcomesFor(trackUid)
}