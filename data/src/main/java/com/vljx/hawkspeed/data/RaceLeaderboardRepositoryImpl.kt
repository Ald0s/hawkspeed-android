package com.vljx.hawkspeed.data

import androidx.paging.PagingData
import com.vljx.hawkspeed.data.mapper.race.RaceLeaderboardMapper
import com.vljx.hawkspeed.data.source.race.RaceLeaderboardLocalData
import com.vljx.hawkspeed.data.source.race.RaceLeaderboardRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.repository.RaceLeaderboardRepository
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.requestmodels.user.RequestPageRaceHistory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RaceLeaderboardRepositoryImpl @Inject constructor(
    private val raceLeaderboardLocalData: RaceLeaderboardLocalData,
    private val raceLeaderboardRemoteData: RaceLeaderboardRemoteData,

    private val raceLeaderboardMapper: RaceLeaderboardMapper
): BaseRepository(), RaceLeaderboardRepository {
    override fun getLeaderboardEntryForRace(requestGetRace: RequestGetRace): Flow<Resource<RaceLeaderboard>> =
        flowQueryFromCacheNetworkAndCache(
            raceLeaderboardMapper,
            databaseQuery = { raceLeaderboardLocalData.selectLeaderboardEntryForRace(requestGetRace) },
            networkQuery = { raceLeaderboardRemoteData.queryLeaderboardEntry(requestGetRace) },
            cacheResult = { raceLeaderboard -> raceLeaderboardLocalData.upsertRaceLeaderboard(raceLeaderboard) }
        )

    override fun getCachedLeaderboardEntryForRace(requestGetRace: RequestGetRace): Flow<RaceLeaderboard?> =
        flowFromCache(
            raceLeaderboardMapper,
            databaseQuery = {
                raceLeaderboardLocalData.selectLeaderboardEntryForRace(requestGetRace)
            }
        )
}