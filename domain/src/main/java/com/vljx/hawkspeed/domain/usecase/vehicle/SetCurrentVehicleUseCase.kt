package com.vljx.hawkspeed.domain.usecase.vehicle

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestSetCurrentVehicle
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class SetCurrentVehicleUseCase @Inject constructor(
    @Bridged
    private val vehicleRepository: VehicleRepository
): BaseSuspendingUseCase<RequestSetCurrentVehicle, Resource<Vehicle>> {
    override suspend fun invoke(params: RequestSetCurrentVehicle): Resource<Vehicle> {
        TODO("Not yet implemented")
    }
}