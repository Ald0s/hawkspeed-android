package com.vljx.hawkspeed.data.mapper.vehicle

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.mapper.user.UserMapper
import com.vljx.hawkspeed.data.mapper.vehicle.stock.VehicleStockMapper
import com.vljx.hawkspeed.data.models.vehicle.VehicleModel
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import javax.inject.Inject

class VehicleMapper @Inject constructor(
    private val vehicleStockMapper: VehicleStockMapper,
    private val userMapper: UserMapper
): Mapper<VehicleModel, Vehicle> {
    override fun mapFromData(model: VehicleModel): Vehicle {
        return Vehicle(
            model.vehicleUid,
            model.title,
            vehicleStockMapper.mapFromData(model.vehicleStock),
            userMapper.mapFromData(model.user)
        )
    }

    override fun mapToData(domain: Vehicle): VehicleModel {
        return VehicleModel(
            domain.vehicleUid,
            domain.title,
            vehicleStockMapper.mapToData(domain.vehicleStock),
            userMapper.mapToData(domain.user)
        )
    }
}