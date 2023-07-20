package com.vljx.hawkspeed.data.network.mapper.vehicle.stock

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleYearsPageModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleYearsPageDto
import javax.inject.Inject

class VehicleYearsPageDtoMapper @Inject constructor(

): DtoMapper<VehicleYearsPageDto, VehicleYearsPageModel> {
    override fun mapFromDto(dto: VehicleYearsPageDto): VehicleYearsPageModel {
        TODO("Not yet implemented")
    }
}