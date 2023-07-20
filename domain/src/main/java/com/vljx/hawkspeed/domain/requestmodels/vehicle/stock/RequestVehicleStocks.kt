package com.vljx.hawkspeed.domain.requestmodels.vehicle.stock

data class RequestVehicleStocks(
    val makeUid: String,
    val typeId: String,
    val modelUid: String,
    val year: Int
)