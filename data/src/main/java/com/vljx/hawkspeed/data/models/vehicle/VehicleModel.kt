package com.vljx.hawkspeed.data.models.vehicle

import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStockModel

data class VehicleModel(
    val vehicleUid: String,
    val title: String,
    val vehicleStock: VehicleStockModel,
    val user: UserModel
)