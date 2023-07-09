package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.race.RaceLeaderboardMapper
import com.vljx.hawkspeed.data.source.race.RaceLeaderboardLocalData
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.repository.RaceLeaderboardRepository
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RaceLeaderboardRepositoryImpl @Inject constructor(
    private val raceLeaderboardLocalData: RaceLeaderboardLocalData,

    private val raceLeaderboardMapper: RaceLeaderboardMapper
): BaseRepository(), RaceLeaderboardRepository {
    override fun getLeaderboardEntryForRace(requestGetRace: RequestGetRace): Flow<RaceLeaderboard?> =
        flowFromCache(
            raceLeaderboardMapper,
            databaseQuery = {
                raceLeaderboardLocalData.selectLeaderboardEntryForRace(requestGetRace)
            }
        )
}