package com.vljx.hawkspeed.domain.usecase.vehicle

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestGetVehicle
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVehicleByUidUseCase @Inject constructor(
    @Bridged
    private val vehicleRepository: VehicleRepository
): BaseUseCase<RequestGetVehicle, Flow<Resource<Vehicle>>> {
    override fun invoke(params: RequestGetVehicle): Flow<Resource<Vehicle>> =
        vehicleRepository.getVehicleByUid(params)
}