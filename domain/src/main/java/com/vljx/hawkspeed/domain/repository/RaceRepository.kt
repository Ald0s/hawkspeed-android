package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import kotlinx.coroutines.flow.Flow

interface RaceRepository {
    /**
     * Select the currently ongoing race from cache, if any.
     */
    fun selectOngoingRace(): Flow<Race?>

    /**
     * Select the desired race from cache.
     */
    fun getRace(requestGetRace: RequestGetRace): Flow<Race?>

    /**
     * Cache the desired race.
     */
    suspend fun cacheRace(race: Race)
}