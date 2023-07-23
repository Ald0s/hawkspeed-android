package com.vljx.hawkspeed.data.network.mapper.vehicle.stock

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleTypesPageModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleTypesPageDto
import javax.inject.Inject

class VehicleTypesPageDtoMapper @Inject constructor(
    private val vehicleTypeDtoMapper: VehicleTypeDtoMapper
): DtoMapper<VehicleTypesPageDto, VehicleTypesPageModel> {
    override fun mapFromDto(dto: VehicleTypesPageDto): VehicleTypesPageModel {
        return VehicleTypesPageModel(
            vehicleTypeDtoMapper.mapFromDtoList(dto.types),
            dto.thisPage,
            dto.nextPage
        )
    }
}