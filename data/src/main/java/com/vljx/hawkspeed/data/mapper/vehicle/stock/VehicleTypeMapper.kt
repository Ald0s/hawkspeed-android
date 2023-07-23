package com.vljx.hawkspeed.data.mapper.vehicle.stock

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleTypeModel
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleType
import javax.inject.Inject

class VehicleTypeMapper @Inject constructor(

): Mapper<VehicleTypeModel, VehicleType> {
    override fun mapFromData(model: VehicleTypeModel): VehicleType {
        return VehicleType(
            model.typeId,
            model.typeName,
            model.description
        )
    }

    override fun mapToData(domain: VehicleType): VehicleTypeModel {
        return VehicleTypeModel(
            domain.typeId,
            domain.typeName,
            domain.description
        )
    }
}