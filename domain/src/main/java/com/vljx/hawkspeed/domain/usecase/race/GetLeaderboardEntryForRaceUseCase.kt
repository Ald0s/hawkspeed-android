package com.vljx.hawkspeed.domain.usecase.race

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.repository.RaceLeaderboardRepository
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLeaderboardEntryForRaceUseCase @Inject constructor(
    @Bridged
    private val raceLeaderboardRepository: RaceLeaderboardRepository
): BaseUseCase<RequestGetRace, Flow<RaceLeaderboard?>> {
    override fun invoke(params: RequestGetRace): Flow<RaceLeaderboard?> =
        raceLeaderboardRepository.getLeaderboardEntryForRace(params)
}