package com.vljx.hawkspeed.data.network.mapper.vehicle

import com.vljx.hawkspeed.data.models.vehicle.OurVehiclesModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.OurVehiclesDto
import javax.inject.Inject

class OurVehiclesDtoMapper @Inject constructor(
    private val vehicleDtoMapper: VehicleDtoMapper
): DtoMapper<OurVehiclesDto, OurVehiclesModel> {
    override fun mapFromDto(dto: OurVehiclesDto): OurVehiclesModel {
        return OurVehiclesModel(
            vehicleDtoMapper.mapFromDtoList(dto.vehicles)
        )
    }
}