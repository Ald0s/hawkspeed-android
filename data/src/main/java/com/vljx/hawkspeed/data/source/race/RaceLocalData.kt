package com.vljx.hawkspeed.data.source.race

import com.vljx.hawkspeed.data.models.race.RaceModel
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import kotlinx.coroutines.flow.Flow

interface RaceLocalData {
    fun selectRace(requestGetRace: RequestGetRace): Flow<RaceModel?>

    suspend fun upsertRace(raceModel: RaceModel)
}