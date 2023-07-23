package com.vljx.hawkspeed.data.network.models.vehicle.stock

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VehicleMakeDto(
    @Expose
    @SerializedName("make_uid")
    val makeUid: String,

    @Expose
    @SerializedName("make_name")
    val makeName: String
)