package com.vljx.hawkspeed.data.network.mapper.vehicle.stock

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleTypesPageModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleTypesPageDto
import javax.inject.Inject

class VehicleTypesPageDtoMapper @Inject constructor(

): DtoMapper<VehicleTypesPageDto, VehicleTypesPageModel> {
    override fun mapFromDto(dto: VehicleTypesPageDto): VehicleTypesPageModel {
        TODO("Not yet implemented")
    }
}