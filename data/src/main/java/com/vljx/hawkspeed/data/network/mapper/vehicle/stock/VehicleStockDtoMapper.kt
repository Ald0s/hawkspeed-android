package com.vljx.hawkspeed.data.network.mapper.vehicle.stock

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStockModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleStockDto
import javax.inject.Inject

class VehicleStockDtoMapper @Inject constructor(
    private val vehicleMakeDtoMapper: VehicleMakeDtoMapper,
    private val vehicleModelDtoMapper: VehicleModelDtoMapper
): DtoMapper<VehicleStockDto, VehicleStockModel> {
    override fun mapFromDto(dto: VehicleStockDto): VehicleStockModel {
        return VehicleStockModel(
            dto.vehicleStockUid,
            vehicleMakeDtoMapper.mapFromDto(dto.make),
            vehicleModelDtoMapper.mapFromDto(dto.model),
            dto.year,
            dto.version,
            dto.badge,
            dto.motorType,
            dto.displacement,
            dto.induction,
            dto.fuelType,
            dto.power,
            dto.electricType,
            dto.transmissionType,
            dto.numGears
        )
    }
}