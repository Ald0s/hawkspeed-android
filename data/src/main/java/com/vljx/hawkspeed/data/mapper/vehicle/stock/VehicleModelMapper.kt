package com.vljx.hawkspeed.data.mapper.vehicle.stock

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleModelModel
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleModel
import javax.inject.Inject

class VehicleModelMapper @Inject constructor(
    private val vehicleTypeMapper: VehicleTypeMapper
): Mapper<VehicleModelModel, VehicleModel> {
    override fun mapFromData(model: VehicleModelModel): VehicleModel {
        return VehicleModel(
            model.modelUid,
            model.modelName,
            model.makeUid,
            vehicleTypeMapper.mapFromData(model.type)
        )
    }

    override fun mapToData(domain: VehicleModel): VehicleModelModel {
        return VehicleModelModel(
            domain.modelUid,
            domain.modelName,
            domain.makeUid,
            vehicleTypeMapper.mapToData(domain.type)
        )
    }
}