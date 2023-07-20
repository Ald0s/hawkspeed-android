package com.vljx.hawkspeed.data.network.mapper.vehicle.stock

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleModelModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleModelDto
import javax.inject.Inject

class VehicleModelDtoMapper @Inject constructor(

): DtoMapper<VehicleModelDto, VehicleModelModel> {
    override fun mapFromDto(dto: VehicleModelDto): VehicleModelModel {
        TODO("Not yet implemented")
    }
}