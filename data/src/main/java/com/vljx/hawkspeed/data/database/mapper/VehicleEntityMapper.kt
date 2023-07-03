package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.VehicleEntity
import com.vljx.hawkspeed.data.models.vehicle.VehicleModel
import javax.inject.Inject

class VehicleEntityMapper @Inject constructor(

): EntityMapper<VehicleEntity, VehicleModel> {
    override fun mapFromEntity(entity: VehicleEntity): VehicleModel {
        return VehicleModel(
            entity.vehicleUid,
            entity.text,
            entity.belongsToYou
        )
    }

    override fun mapToEntity(model: VehicleModel): VehicleEntity {
        return VehicleEntity(
            model.vehicleUid,
            model.text,
            model.belongsToYou
        )
    }
}