package com.vljx.hawkspeed.data.network.mapper.vehicle.stock

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleMakeModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleMakeDto
import javax.inject.Inject

class VehicleMakeDtoMapper @Inject constructor(

): DtoMapper<VehicleMakeDto, VehicleMakeModel> {
    override fun mapFromDto(dto: VehicleMakeDto): VehicleMakeModel {
        TODO("Not yet implemented")
    }
}