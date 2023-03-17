package com.vljx.hawkspeed.data.source

import com.vljx.hawkspeed.data.models.race.RaceModel
import com.vljx.hawkspeed.domain.requests.race.GetRaceRequest
import kotlinx.coroutines.flow.Flow

interface RaceLocalData {
    fun selectRace(getRaceRequest: GetRaceRequest): Flow<RaceModel?>

    suspend fun upsertRace(raceModel: RaceModel)
}