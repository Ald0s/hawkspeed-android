package com.vljx.hawkspeed.data.source.race

import com.vljx.hawkspeed.data.models.race.RaceModel
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import kotlinx.coroutines.flow.Flow

interface RaceLocalData {
    /**
     * Open a flow for the desired race.
     */
    fun selectRace(requestGetRace: RequestGetRace): Flow<RaceModel?>

    /**
     * Upsert the given race into cache.
     */
    suspend fun upsertRace(race: RaceModel)
}