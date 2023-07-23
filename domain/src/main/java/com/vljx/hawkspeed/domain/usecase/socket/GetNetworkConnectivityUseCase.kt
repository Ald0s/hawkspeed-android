package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetNetworkConnectivityUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<Unit, StateFlow<Boolean>> {
    override fun invoke(params: Unit): StateFlow<Boolean> =
        worldSocketRepository.networkConnectivity
}