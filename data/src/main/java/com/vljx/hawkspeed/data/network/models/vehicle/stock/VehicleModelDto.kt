package com.vljx.hawkspeed.data.network.models.vehicle.stock

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VehicleModelDto(
    @Expose
    @SerializedName("model_uid")
    val modelUid: String,

    @Expose
    @SerializedName("model_name")
    val modelName: String,

    @Expose
    @SerializedName("make_uid")
    val makeUid: String,

    @Expose
    @SerializedName("type")
    val type: VehicleTypeDto
)