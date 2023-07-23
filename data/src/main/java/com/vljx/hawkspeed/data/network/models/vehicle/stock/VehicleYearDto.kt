package com.vljx.hawkspeed.data.network.models.vehicle.stock

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VehicleYearDto(
    @Expose
    @SerializedName("year")
    val year: Int
)