package com.vljx.hawkspeed.data.network.models.vehicle

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class OurVehiclesDto(
    @Expose
    @SerializedName("items")
    val vehicles: List<VehicleDto>
)