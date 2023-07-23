package com.vljx.hawkspeed.data.network.mapper.vehicle.stock

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleMakesPageModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleMakesPageDto
import javax.inject.Inject

class VehicleMakesPageDtoMapper @Inject constructor(
    private val vehicleMakeDtoMapper: VehicleMakeDtoMapper
): DtoMapper<VehicleMakesPageDto, VehicleMakesPageModel> {
    override fun mapFromDto(dto: VehicleMakesPageDto): VehicleMakesPageModel {
        return VehicleMakesPageModel(
            vehicleMakeDtoMapper.mapFromDtoList(dto.makes),
            dto.thisPage,
            dto.nextPage
        )
    }
}