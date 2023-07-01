package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.race.RaceMapper
import com.vljx.hawkspeed.data.models.race.RaceModel
import com.vljx.hawkspeed.data.source.race.RaceLocalData
import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.repository.RaceRepository
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RaceRepositoryImpl @Inject constructor(
    private val raceLocalData: RaceLocalData,

    private val raceMapper: RaceMapper
): BaseRepository(), RaceRepository {
    override fun selectOngoingRace(): Flow<Race?> {
        TODO("Not yet implemented")
    }

    override fun getRace(requestGetRace: RequestGetRace): Flow<Race?> =
        flowFromCache(
            raceMapper,
            databaseQuery = { raceLocalData.selectRace(requestGetRace) }
        )

    override suspend fun cacheRace(race: Race) {
        // Map from domain model to data model.
        val raceModel: RaceModel = raceMapper.mapToData(race)
        // Upsert.
        raceLocalData.upsertRace(raceModel)
    }
}