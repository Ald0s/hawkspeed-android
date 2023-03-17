package com.vljx.hawkspeed.domain.interactor.race

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.interactor.BaseUseCase
import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.repository.RaceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOngoingRaceUseCase @Inject constructor(
    @Bridged
    private val raceRepository: RaceRepository
): BaseUseCase<Unit, Flow<Race?>> {
    override fun invoke(params: Unit): Flow<Race?> =
        raceRepository.selectOngoingRace()
}