package com.vljx.hawkspeed.data.network.requestmodels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestCreateVehicle

data class RequestCreateVehicleDto(
    @Expose
    @SerializedName("vehicle_stock_uid")
    val vehicleStockUid: String
) {
    constructor(requestCreateVehicle: RequestCreateVehicle):
            this(
                requestCreateVehicle.vehicleStockUid
            )
}