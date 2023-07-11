package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestUpdateMagnetometerReadings
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import javax.inject.Inject

class UpdateMagnetometerReadingsUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<RequestUpdateMagnetometerReadings, Unit> {
    override fun invoke(params: RequestUpdateMagnetometerReadings) =
        worldSocketRepository.updateMagnetometerReadings(params)
}