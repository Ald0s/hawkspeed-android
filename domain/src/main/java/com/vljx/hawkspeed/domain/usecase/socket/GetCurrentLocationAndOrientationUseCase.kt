package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetCurrentLocationAndOrientationUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<Unit, Flow<PlayerPositionWithOrientation?>> {
    override fun invoke(params: Unit): Flow<PlayerPositionWithOrientation?> =
        combine(
            worldSocketRepository.currentLocation,
            worldSocketRepository.latestOrientationAngles
        ) { playerPosition, deviceOrientation ->
            if(playerPosition == null) {
                return@combine null
            }
            return@combine PlayerPositionWithOrientation(
                playerPosition,
                deviceOrientation
            )
        }
}