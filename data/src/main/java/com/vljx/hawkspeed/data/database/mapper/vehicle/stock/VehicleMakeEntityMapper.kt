package com.vljx.hawkspeed.data.database.mapper.vehicle.stock

import com.vljx.hawkspeed.data.database.entity.vehicle.stock.VehicleMakeEntity
import com.vljx.hawkspeed.data.database.mapper.EntityMapper
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleMakeModel
import javax.inject.Inject

class VehicleMakeEntityMapper @Inject constructor(

): EntityMapper<VehicleMakeEntity, VehicleMakeModel> {
    override fun mapFromEntity(entity: VehicleMakeEntity): VehicleMakeModel {
        return VehicleMakeModel(
            entity.vehicleMakeUid,
            entity.makeName,
            entity.logoUrl
        )
    }

    override fun mapToEntity(model: VehicleMakeModel): VehicleMakeEntity {
        return VehicleMakeEntity(
            model.makeUid,
            model.makeName,
            model.logoUrl
        )
    }
}