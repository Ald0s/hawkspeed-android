package com.vljx.hawkspeed.domain.usecase.vehicle.stock

import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleYear
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleYears
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PageVehicleYearsUseCase @Inject constructor(
    @Bridged
    private val vehicleRepository: VehicleRepository
): BaseUseCase<RequestVehicleYears, Flow<PagingData<VehicleYear>>> {
    override fun invoke(params: RequestVehicleYears): Flow<PagingData<VehicleYear>> =
        vehicleRepository.pageVehicleYears(params)
}