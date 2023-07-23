package com.vljx.hawkspeed.domain.usecase.vehicle.stock

import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleType
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleTypes
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PageVehicleTypesUseCase @Inject constructor(
    @Bridged
    private val vehicleRepository: VehicleRepository
): BaseUseCase<RequestVehicleTypes, Flow<PagingData<VehicleType>>> {
    override fun invoke(params: RequestVehicleTypes): Flow<PagingData<VehicleType>> =
        vehicleRepository.pageVehicleTypes(params)
}