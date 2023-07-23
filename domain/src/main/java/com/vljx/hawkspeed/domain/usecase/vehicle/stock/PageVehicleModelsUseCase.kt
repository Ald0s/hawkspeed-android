package com.vljx.hawkspeed.domain.usecase.vehicle.stock

import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleModel
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleModels
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PageVehicleModelsUseCase @Inject constructor(
    @Bridged
    private val vehicleRepository: VehicleRepository
): BaseUseCase<RequestVehicleModels, Flow<PagingData<VehicleModel>>> {
    override fun invoke(params: RequestVehicleModels): Flow<PagingData<VehicleModel>> =
        vehicleRepository.pageVehicleModels(params)
}