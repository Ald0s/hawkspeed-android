package com.vljx.hawkspeed.data.mapper.vehicle.stock

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStockModel
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleStock
import javax.inject.Inject

class VehicleStockMapper @Inject constructor(
    private val vehicleMakeMapper: VehicleMakeMapper,
    private val vehicleModelMapper: VehicleModelMapper
): Mapper<VehicleStockModel, VehicleStock> {
    override fun mapFromData(model: VehicleStockModel): VehicleStock {
        return VehicleStock(
            model.vehicleStockUid,
            vehicleMakeMapper.mapFromData(model.make),
            vehicleModelMapper.mapFromData(model.model),
            model.year,
            model.version,
            model.badge,
            model.motorType,
            model.displacement,
            model.induction,
            model.fuelType,
            model.power,
            model.electricType,
            model.transmissionType,
            model.numGears
        )
    }

    override fun mapToData(domain: VehicleStock): VehicleStockModel {
        return VehicleStockModel(
            domain.vehicleStockUid,
            vehicleMakeMapper.mapToData(domain.make),
            vehicleModelMapper.mapToData(domain.model),
            domain.year,
            domain.version,
            domain.badge,
            domain.motorType,
            domain.displacement,
            domain.induction,
            domain.fuelType,
            domain.power,
            domain.electricType,
            domain.transmissionType,
            domain.numGears
        )
    }
}