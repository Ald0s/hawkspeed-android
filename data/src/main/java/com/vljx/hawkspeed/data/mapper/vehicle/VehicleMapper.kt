package com.vljx.hawkspeed.data.mapper.vehicle

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.vehicle.VehicleModel
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import javax.inject.Inject

class VehicleMapper @Inject constructor(

): Mapper<VehicleModel, Vehicle> {
    override fun mapFromData(model: VehicleModel): Vehicle {
        return Vehicle(
            model.vehicleUid,
            model.text,
            model.belongsToYou
        )
    }

    override fun mapToData(domain: Vehicle): VehicleModel {
        return VehicleModel(
            domain.vehicleUid,
            domain.text,
            domain.belongsToYou
        )
    }
}