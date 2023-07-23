package com.vljx.hawkspeed.data.database.mapper.vehicle.stock

import com.vljx.hawkspeed.data.database.entity.vehicle.stock.VehicleTypeEntity
import com.vljx.hawkspeed.data.database.mapper.EntityMapper
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleTypeModel
import javax.inject.Inject

class VehicleTypeEntityMapper @Inject constructor(

): EntityMapper<VehicleTypeEntity, VehicleTypeModel> {
    override fun mapFromEntity(entity: VehicleTypeEntity): VehicleTypeModel {
        return VehicleTypeModel(
            entity.typeId,
            entity.typeName,
            entity.description
        )
    }

    override fun mapToEntity(model: VehicleTypeModel): VehicleTypeEntity {
        return VehicleTypeEntity(
            model.typeId,
            model.typeName,
            model.description
        )
    }
}