package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.models.world.LocationUpdateRate
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import javax.inject.Inject

class SetLocationUpdateRateUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<LocationUpdateRate, Unit> {
    override fun invoke(params: LocationUpdateRate) =
        worldSocketRepository.setLocationUpdateRate(params)
}