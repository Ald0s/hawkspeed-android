package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.models.race.Race
import kotlinx.coroutines.flow.Flow

interface RaceRepository {
    fun selectOngoingRace(): Flow<Race>

    suspend fun cacheRace(race: Race)
}