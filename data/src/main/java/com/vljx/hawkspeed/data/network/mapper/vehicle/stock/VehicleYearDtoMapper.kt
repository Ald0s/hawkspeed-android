package com.vljx.hawkspeed.data.network.mapper.vehicle.stock

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleYearModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleYearDto
import javax.inject.Inject

class VehicleYearDtoMapper @Inject constructor(

): DtoMapper<VehicleYearDto, VehicleYearModel> {
    override fun mapFromDto(dto: VehicleYearDto): VehicleYearModel {
        return VehicleYearModel(
            dto.year
        )
    }
}