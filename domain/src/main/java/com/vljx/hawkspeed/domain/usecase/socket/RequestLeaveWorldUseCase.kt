package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestLeaveWorld
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import javax.inject.Inject

class RequestLeaveWorldUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<RequestLeaveWorld, Unit> {
    override fun invoke(params: RequestLeaveWorld) =
        worldSocketRepository.requestLeaveWorld(params)
}