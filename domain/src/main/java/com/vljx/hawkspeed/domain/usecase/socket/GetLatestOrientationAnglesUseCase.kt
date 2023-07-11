package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.models.world.DeviceOrientation
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLatestOrientationAnglesUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<Unit, Flow<DeviceOrientation>> {
    override fun invoke(params: Unit): Flow<DeviceOrientation> =
        worldSocketRepository.latestOrientationAngles
}