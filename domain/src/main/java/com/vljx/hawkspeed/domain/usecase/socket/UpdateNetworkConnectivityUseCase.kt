package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateNetworkConnectivity
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import javax.inject.Inject

class UpdateNetworkConnectivityUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<RequestUpdateNetworkConnectivity, Unit> {
    override fun invoke(params: RequestUpdateNetworkConnectivity) =
        worldSocketRepository.updateNetworkConnectivity(params)
}