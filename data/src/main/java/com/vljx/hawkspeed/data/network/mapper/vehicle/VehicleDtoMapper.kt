package com.vljx.hawkspeed.data.network.mapper.vehicle

import com.vljx.hawkspeed.data.models.vehicle.VehicleModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.mapper.user.UserDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.stock.VehicleStockDtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.VehicleDto
import javax.inject.Inject

class VehicleDtoMapper @Inject constructor(
    private val vehicleStockDtoMapper: VehicleStockDtoMapper,
    private val userDtoMapper: UserDtoMapper
): DtoMapper<VehicleDto, VehicleModel> {
    override fun mapFromDto(dto: VehicleDto): VehicleModel {
        return VehicleModel(
            dto.vehicleUid,
            dto.title,
            vehicleStockDtoMapper.mapFromDto(dto.vehicleStock),
            userDtoMapper.mapFromDto(dto.user)
        )
    }
}