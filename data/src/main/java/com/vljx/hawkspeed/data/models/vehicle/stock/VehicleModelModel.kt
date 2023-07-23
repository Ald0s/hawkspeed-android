package com.vljx.hawkspeed.data.models.vehicle.stock

data class VehicleModelModel(
    val modelUid: String,
    val modelName: String,
    val makeUid: String,
    val type: VehicleTypeModel
)