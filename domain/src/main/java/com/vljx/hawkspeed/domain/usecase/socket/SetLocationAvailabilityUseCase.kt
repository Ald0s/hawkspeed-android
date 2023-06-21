package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import javax.inject.Inject

class SetLocationAvailabilityUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<Boolean, Unit> {
    override fun invoke(params: Boolean) =
        worldSocketRepository.setLocationAvailability(params)
}