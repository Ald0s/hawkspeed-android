package com.vljx.hawkspeed.domain.usecase.race

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.repository.RaceLeaderboardRepository
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Open a flow for a race leaderboard entry, only from cache. This is intended to be used to trigger a race completion, for when this flow emits a non-null
 * value, any collectors can be sure the race has a completed outcome.
 */
class GetCachedLeaderboardEntryForRaceUseCase @Inject constructor(
    @Bridged
    private val raceLeaderboardRepository: RaceLeaderboardRepository
): BaseUseCase<RequestGetRace, Flow<RaceLeaderboard?>> {
    override fun invoke(params: RequestGetRace): Flow<RaceLeaderboard?> =
        raceLeaderboardRepository.getCachedLeaderboardEntryForRace(params)
}