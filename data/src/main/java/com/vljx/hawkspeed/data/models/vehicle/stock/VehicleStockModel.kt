package com.vljx.hawkspeed.data.models.vehicle.stock

data class VehicleStockModel(
    val vehicleStockUid: String,
    val make: VehicleMakeModel,
    val model: VehicleModelModel,
    val year: Int,
    val version: String?,
    val badge: String?,
    val motorType: String,
    val displacement: Int,
    val induction: String?,
    val fuelType: String?,
    val power: Int?,
    val electricType: String?,
    val transmissionType: String?,
    val numGears: Int?
)