package com.vljx.hawkspeed.data.mapper.vehicle.stock

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleMakeModel
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleMake
import javax.inject.Inject

class VehicleMakeMapper @Inject constructor(

): Mapper<VehicleMakeModel, VehicleMake> {
    override fun mapFromData(model: VehicleMakeModel): VehicleMake {
        return VehicleMake(
            model.makeUid,
            model.makeName,
            model.logoUrl
        )
    }

    override fun mapToData(domain: VehicleMake): VehicleMakeModel {
        return VehicleMakeModel(
            domain.makeUid,
            domain.makeName,
            domain.logoUrl
        )
    }
}