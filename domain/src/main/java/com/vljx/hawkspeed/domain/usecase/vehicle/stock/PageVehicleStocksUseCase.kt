package com.vljx.hawkspeed.domain.usecase.vehicle.stock

import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleStock
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleStocks
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PageVehicleStocksUseCase @Inject constructor(
    @Bridged
    private val vehicleRepository: VehicleRepository
): BaseUseCase<RequestVehicleStocks, Flow<PagingData<VehicleStock>>> {
    override fun invoke(params: RequestVehicleStocks): Flow<PagingData<VehicleStock>> =
        vehicleRepository.pageVehicleStocks(params)
}