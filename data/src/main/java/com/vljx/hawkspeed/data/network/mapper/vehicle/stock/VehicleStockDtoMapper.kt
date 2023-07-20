package com.vljx.hawkspeed.data.network.mapper.vehicle.stock

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStockModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleStockDto
import javax.inject.Inject

class VehicleStockDtoMapper @Inject constructor(

): DtoMapper<VehicleStockDto, VehicleStockModel> {
    override fun mapFromDto(dto: VehicleStockDto): VehicleStockModel {
        TODO("Not yet implemented")
    }
}