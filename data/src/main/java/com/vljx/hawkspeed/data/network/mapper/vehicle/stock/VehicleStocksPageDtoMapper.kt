package com.vljx.hawkspeed.data.network.mapper.vehicle.stock

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStocksPageModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleStocksPageDto
import javax.inject.Inject

class VehicleStocksPageDtoMapper @Inject constructor(

): DtoMapper<VehicleStocksPageDto, VehicleStocksPageModel> {
    override fun mapFromDto(dto: VehicleStocksPageDto): VehicleStocksPageModel {
        TODO("Not yet implemented")
    }
}