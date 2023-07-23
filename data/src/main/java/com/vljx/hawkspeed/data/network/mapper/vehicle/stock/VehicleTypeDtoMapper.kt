package com.vljx.hawkspeed.data.network.mapper.vehicle.stock

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleTypeModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleTypeDto
import javax.inject.Inject

class VehicleTypeDtoMapper @Inject constructor(

): DtoMapper<VehicleTypeDto, VehicleTypeModel> {
    override fun mapFromDto(dto: VehicleTypeDto): VehicleTypeModel {
        return VehicleTypeModel(
            dto.typeId,
            dto.typeName,
            dto.description
        )
    }
}