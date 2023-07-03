package com.vljx.hawkspeed.domain.usecase.vehicle

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestDeleteVehicle
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class DeleteVehicleByUidUseCase @Inject constructor(
    @Bridged
    private val vehicleRepository: VehicleRepository
): BaseSuspendingUseCase<RequestDeleteVehicle, Unit> {
    override suspend fun invoke(params: RequestDeleteVehicle) =
        vehicleRepository.deleteVehicleByUid(params)
}