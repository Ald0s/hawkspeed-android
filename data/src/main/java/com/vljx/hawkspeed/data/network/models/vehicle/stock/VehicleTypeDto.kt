package com.vljx.hawkspeed.data.network.models.vehicle.stock

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VehicleTypeDto(
    @Expose
    @SerializedName("type_id")
    val typeId: String,

    @Expose
    @SerializedName("name")
    val typeName: String,

    @Expose
    @SerializedName("description")
    val description: String
)