package com.vljx.hawkspeed.data.database.mapper.vehicle.stock

import com.vljx.hawkspeed.data.database.entity.vehicle.stock.VehicleModelEntity
import com.vljx.hawkspeed.data.database.mapper.EntityMapper
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleModelModel
import javax.inject.Inject

class VehicleModelEntityMapper @Inject constructor(
    private val vehicleTypeEntityMapper: VehicleTypeEntityMapper
): EntityMapper<VehicleModelEntity, VehicleModelModel> {
    override fun mapFromEntity(entity: VehicleModelEntity): VehicleModelModel {
        return VehicleModelModel(
            entity.vehicleModelUid,
            entity.modelName,
            entity.makeUid,
            vehicleTypeEntityMapper.mapFromEntity(entity.type)
        )
    }

    override fun mapToEntity(model: VehicleModelModel): VehicleModelEntity {
        return VehicleModelEntity(
            model.modelUid,
            model.modelName,
            model.makeUid,
            vehicleTypeEntityMapper.mapToEntity(model.type)
        )
    }
}