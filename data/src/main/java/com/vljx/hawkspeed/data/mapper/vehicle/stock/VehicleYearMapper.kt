package com.vljx.hawkspeed.data.mapper.vehicle.stock

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleYearModel
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleYear
import javax.inject.Inject

class VehicleYearMapper @Inject constructor(

): Mapper<VehicleYearModel, VehicleYear> {
    override fun mapFromData(model: VehicleYearModel): VehicleYear {
        return VehicleYear(
            model.year
        )
    }

    override fun mapToData(domain: VehicleYear): VehicleYearModel {
        return VehicleYearModel(
            domain.year
        )
    }
}