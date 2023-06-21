package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.models.world.ViewportUpdateResult
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestViewportUpdate
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class SendViewportUpdateUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseSuspendingUseCase<RequestViewportUpdate, ViewportUpdateResult> {
    override suspend fun invoke(params: RequestViewportUpdate): ViewportUpdateResult =
        worldSocketRepository.sendViewportUpdate(params)
}