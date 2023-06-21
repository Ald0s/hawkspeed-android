package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.models.world.StartRaceResult
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.requestmodels.race.RequestStartRace
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class SendStartRaceRequestUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseSuspendingUseCase<RequestStartRace, StartRaceResult> {
    override suspend fun invoke(params: RequestStartRace): StartRaceResult =
        worldSocketRepository.startRace(params)
}