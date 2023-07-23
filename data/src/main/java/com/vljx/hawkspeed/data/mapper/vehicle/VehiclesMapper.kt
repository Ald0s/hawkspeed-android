package com.vljx.hawkspeed.data.mapper.vehicle

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.mapper.user.UserMapper
import com.vljx.hawkspeed.data.models.vehicle.VehiclesModel
import com.vljx.hawkspeed.domain.models.vehicle.Vehicles
import javax.inject.Inject

class VehiclesMapper @Inject constructor(
    private val userMapper: UserMapper,
    private val vehicleMapper: VehicleMapper
): Mapper<VehiclesModel, Vehicles> {
    override fun mapFromData(model: VehiclesModel): Vehicles {
        TODO("Not yet implemented")
    }

    override fun mapToData(domain: Vehicles): VehiclesModel {
        TODO("Not yet implemented")
    }
}