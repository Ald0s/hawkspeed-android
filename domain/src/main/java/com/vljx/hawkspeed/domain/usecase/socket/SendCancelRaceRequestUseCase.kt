package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.models.race.CancelRaceResult
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.requestmodels.race.RequestCancelRace
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class SendCancelRaceRequestUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseSuspendingUseCase<RequestCancelRace, CancelRaceResult> {
    override suspend fun invoke(params: RequestCancelRace): CancelRaceResult =
        worldSocketRepository.cancelRace(params)
}