package com.vljx.hawkspeed.domain.usecase.vehicle

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.vehicle.OurVehicles
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOurVehiclesUseCase @Inject constructor(
    @Bridged
    private val vehicleRepository: VehicleRepository
): BaseUseCase<Unit, Flow<Resource<OurVehicles>>> {
    override fun invoke(params: Unit): Flow<Resource<OurVehicles>> =
        vehicleRepository.getOurVehicles()
}