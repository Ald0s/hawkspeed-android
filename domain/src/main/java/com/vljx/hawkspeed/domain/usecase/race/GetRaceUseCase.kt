package com.vljx.hawkspeed.domain.usecase.race

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.repository.RaceRepository
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRaceUseCase @Inject constructor(
    @Bridged
    private val raceRepository: RaceRepository
): BaseUseCase<RequestGetRace, Flow<Race?>> {
    override fun invoke(params: RequestGetRace): Flow<Race?> =
        raceRepository.getRace(params)
}