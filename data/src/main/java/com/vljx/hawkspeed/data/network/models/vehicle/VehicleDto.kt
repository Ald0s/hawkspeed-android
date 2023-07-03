package com.vljx.hawkspeed.data.network.models.vehicle

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VehicleDto(
    @Expose
    @SerializedName("uid")
    val vehicleUid: String,

    @Expose
    @SerializedName("text")
    val text: String,

    @Expose
    @SerializedName("belongs_to_you")
    val belongsToYou: Boolean
)