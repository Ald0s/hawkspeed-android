package com.vljx.hawkspeed.data.database

import com.vljx.hawkspeed.data.database.dao.RaceDao
import com.vljx.hawkspeed.data.database.entity.race.RaceEntity
import com.vljx.hawkspeed.data.database.mapper.RaceEntityMapper
import com.vljx.hawkspeed.data.models.race.RaceModel
import com.vljx.hawkspeed.data.source.race.RaceLocalData
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RaceLocalDataImpl @Inject constructor(
    private val raceDao: RaceDao,

    private val raceEntityMapper: RaceEntityMapper
): RaceLocalData {
    override fun selectRace(requestGetRace: RequestGetRace): Flow<RaceModel?> {
        // Get the query for the race entity, as a flow.
        val raceEntityFlow: Flow<RaceEntity?> = raceDao.selectRace(requestGetRace.raceUid)
        // Return a map for this entity flow to a model flow.
        return raceEntityFlow.map { raceEntity ->
            raceEntity?.run { raceEntityMapper.mapFromEntity(raceEntity) }
        }
    }

    override suspend fun upsertRace(race: RaceModel) {
        // Map the race model to an entity.
        val raceEntity: RaceEntity = raceEntityMapper.mapToEntity(race)
        // Upsert the race entity.
        raceDao.upsert(raceEntity)
    }
}