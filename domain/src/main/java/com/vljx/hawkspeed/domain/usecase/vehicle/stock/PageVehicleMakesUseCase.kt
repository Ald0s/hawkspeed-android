package com.vljx.hawkspeed.domain.usecase.vehicle.stock

import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleMake
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleMakes
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PageVehicleMakesUseCase @Inject constructor(
    @Bridged
    private val vehicleRepository: VehicleRepository
): BaseUseCase<RequestVehicleMakes, Flow<PagingData<VehicleMake>>> {
    override fun invoke(params: RequestVehicleMakes): Flow<PagingData<VehicleMake>> =
        vehicleRepository.pageVehicleMakes(params)
}