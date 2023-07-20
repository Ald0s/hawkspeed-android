package com.vljx.hawkspeed.data.network.mapper.vehicle

import com.vljx.hawkspeed.data.models.vehicle.VehiclesModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.mapper.user.UserDtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.VehicleDto
import com.vljx.hawkspeed.data.network.models.vehicle.VehiclesDto
import javax.inject.Inject

class VehiclesDtoMapper @Inject constructor(
    private val userDtoMapper: UserDtoMapper,
    private val vehicleDtoMapper: VehicleDtoMapper
): DtoMapper<VehiclesDto, VehiclesModel> {
    override fun mapFromDto(dto: VehiclesDto): VehiclesModel {
        return VehiclesModel(
            userDtoMapper.mapFromDto(dto.user),
            vehicleDtoMapper.mapFromDtoList(dto.vehicles)
        )
    }
}