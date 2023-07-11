package com.vljx.hawkspeed.domain.usecase.race

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.repository.RaceLeaderboardRepository
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Open a flow for the request race outcome, this version will select from cache but will also query for the latest version from server.
 */
class GetLeaderboardEntryForRaceUseCase @Inject constructor(
    @Bridged
    private val raceLeaderboardRepository: RaceLeaderboardRepository
): BaseUseCase<RequestGetRace, Flow<Resource<RaceLeaderboard>>> {
    override fun invoke(params: RequestGetRace): Flow<Resource<RaceLeaderboard>> =
        raceLeaderboardRepository.getLeaderboardEntryForRace(params)
}