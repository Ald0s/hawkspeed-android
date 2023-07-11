package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateAccelerometerReadings
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import javax.inject.Inject

class UpdateAccelerometerReadingsUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<RequestUpdateAccelerometerReadings, Unit> {
    override fun invoke(params: RequestUpdateAccelerometerReadings) =
        worldSocketRepository.updateAccelerometerReadings(params)
}