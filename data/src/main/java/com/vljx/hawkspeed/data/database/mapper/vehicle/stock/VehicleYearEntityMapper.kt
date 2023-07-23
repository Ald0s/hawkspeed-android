package com.vljx.hawkspeed.data.database.mapper.vehicle.stock

import com.vljx.hawkspeed.data.database.entity.vehicle.stock.VehicleYearEntity
import com.vljx.hawkspeed.data.database.mapper.EntityMapper
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleYearModel
import javax.inject.Inject

class VehicleYearEntityMapper @Inject constructor(

): EntityMapper<VehicleYearEntity, VehicleYearModel> {
    override fun mapFromEntity(entity: VehicleYearEntity): VehicleYearModel {
        return VehicleYearModel(
            entity.year
        )
    }

    override fun mapToEntity(model: VehicleYearModel): VehicleYearEntity {
        return VehicleYearEntity(
            model.year
        )
    }
}