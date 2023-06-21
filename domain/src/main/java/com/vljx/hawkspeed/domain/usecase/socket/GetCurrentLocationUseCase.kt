package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<Unit, StateFlow<PlayerPosition?>> {
    override fun invoke(params: Unit): StateFlow<PlayerPosition?> =
        worldSocketRepository.currentLocation
}