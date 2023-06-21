package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.models.world.PlayerUpdateResult
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class SendPlayerUpdateUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseSuspendingUseCase<RequestPlayerUpdate, PlayerUpdateResult> {
    override suspend fun invoke(params: RequestPlayerUpdate): PlayerUpdateResult =
        worldSocketRepository.sendPlayerUpdate(params)
}