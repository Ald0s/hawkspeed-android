package com.vljx.hawkspeed.data.database.mapper.vehicle

import com.vljx.hawkspeed.data.database.entity.vehicle.VehicleEntity
import com.vljx.hawkspeed.data.database.mapper.EntityMapper
import com.vljx.hawkspeed.data.database.mapper.UserEntityMapper
import com.vljx.hawkspeed.data.database.mapper.vehicle.stock.VehicleStockEntityMapper
import com.vljx.hawkspeed.data.models.vehicle.VehicleModel
import javax.inject.Inject

class VehicleEntityMapper @Inject constructor(
    private val vehicleStockEntityMapper: VehicleStockEntityMapper,
    private val userEntityMapper: UserEntityMapper
): EntityMapper<VehicleEntity, VehicleModel> {
    override fun mapFromEntity(entity: VehicleEntity): VehicleModel {
        return VehicleModel(
            entity.vehicleUid,
            entity.title,
            vehicleStockEntityMapper.mapFromEntity(entity.vehicleStock),
            userEntityMapper.mapFromEntity(entity.user)
        )
    }

    override fun mapToEntity(model: VehicleModel): VehicleEntity {
        return VehicleEntity(
            model.vehicleUid,
            model.title,
            vehicleStockEntityMapper.mapToEntity(model.vehicleStock),
            userEntityMapper.mapToEntity(model.user)
        )
    }
}