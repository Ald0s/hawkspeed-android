package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestJoinWorld
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import javax.inject.Inject

class RequestJoinWorldUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<RequestJoinWorld, Unit> {
    override fun invoke(params: RequestJoinWorld) =
        worldSocketRepository.requestJoinWorld(params)
}