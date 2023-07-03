package com.vljx.hawkspeed.data.mapper.vehicle

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.vehicle.OurVehiclesModel
import com.vljx.hawkspeed.domain.models.vehicle.OurVehicles
import javax.inject.Inject

class OurVehiclesMapper @Inject constructor(
    private val vehicleMapper: VehicleMapper
): Mapper<OurVehiclesModel, OurVehicles> {
    override fun mapFromData(model: OurVehiclesModel): OurVehicles {
        return OurVehicles(
            vehicleMapper.mapFromDataList(model.vehicles)
        )
    }

    override fun mapToData(domain: OurVehicles): OurVehiclesModel {
        return OurVehiclesModel(
            vehicleMapper.mapToDataList(domain.vehicles)
        )
    }
}