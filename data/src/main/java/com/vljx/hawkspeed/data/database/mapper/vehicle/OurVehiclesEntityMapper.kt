package com.vljx.hawkspeed.data.database.mapper.vehicle

import com.vljx.hawkspeed.data.database.entity.vehicle.OurVehiclesEntity
import com.vljx.hawkspeed.data.database.mapper.EntityMapper
import com.vljx.hawkspeed.data.models.vehicle.OurVehiclesModel
import javax.inject.Inject

class OurVehiclesEntityMapper @Inject constructor(
    private val vehicleEntityMapper: VehicleEntityMapper
): EntityMapper<OurVehiclesEntity, OurVehiclesModel> {
    override fun mapFromEntity(entity: OurVehiclesEntity): OurVehiclesModel {
        return OurVehiclesModel(
            vehicleEntityMapper.mapFromEntityList(entity.vehicles)
        )
    }

    override fun mapToEntity(model: OurVehiclesModel): OurVehiclesEntity {
        return OurVehiclesEntity(
            vehicleEntityMapper.mapToEntityList(model.vehicles)
        )
    }
}