package com.vljx.hawkspeed.data.database.mapper.vehicle.stock

import com.vljx.hawkspeed.data.database.entity.vehicle.stock.VehicleStockEntity
import com.vljx.hawkspeed.data.database.mapper.EntityMapper
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStockModel
import javax.inject.Inject

class VehicleStockEntityMapper @Inject constructor(
    private val vehicleMakeEntityMapper: VehicleMakeEntityMapper,
    private val vehicleModelEntityMapper: VehicleModelEntityMapper
): EntityMapper<VehicleStockEntity, VehicleStockModel> {
    override fun mapFromEntity(entity: VehicleStockEntity): VehicleStockModel {
        return VehicleStockModel(
            entity.vehicleStockUid,
            vehicleMakeEntityMapper.mapFromEntity(entity.make),
            vehicleModelEntityMapper.mapFromEntity(entity.model),
            entity.year,
            entity.version,
            entity.badge,
            entity.motorType,
            entity.displacement,
            entity.induction,
            entity.fuelType,
            entity.power,
            entity.electricType,
            entity.transmissionType,
            entity.numGears
        )
    }

    override fun mapToEntity(model: VehicleStockModel): VehicleStockEntity {
        return VehicleStockEntity(
            model.vehicleStockUid,
            vehicleMakeEntityMapper.mapToEntity(model.make),
            vehicleModelEntityMapper.mapToEntity(model.model),
            model.year,
            model.version,
            model.badge,
            model.motorType,
            model.displacement,
            model.induction,
            model.fuelType,
            model.power,
            model.electricType,
            model.transmissionType,
            model.numGears
        )
    }
}