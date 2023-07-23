package com.vljx.hawkspeed.domain.usecase.vehicle.stock

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleStock
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVehicleStockFromCacheUseCase @Inject constructor(
    @Bridged
    private val vehicleRepository: VehicleRepository
): BaseUseCase<String, Flow<VehicleStock?>> {
    override fun invoke(params: String): Flow<VehicleStock?> =
        vehicleRepository.getVehicleStockFromCache(params)
}