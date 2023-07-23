package com.vljx.hawkspeed.data.network.mapper.vehicle.stock

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleModelsPageModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleModelsPageDto
import javax.inject.Inject

class VehicleModelsPageDtoMapper @Inject constructor(
    private val vehicleModelDtoMapper: VehicleModelDtoMapper
): DtoMapper<VehicleModelsPageDto, VehicleModelsPageModel> {
    override fun mapFromDto(dto: VehicleModelsPageDto): VehicleModelsPageModel {
        return VehicleModelsPageModel(
            vehicleModelDtoMapper.mapFromDtoList(dto.models),
            dto.thisPage,
            dto.nextPage
        )
    }
}