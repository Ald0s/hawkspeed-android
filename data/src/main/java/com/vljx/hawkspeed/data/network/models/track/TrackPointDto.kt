package com.vljx.hawkspeed.data.network.models.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TrackPointDto(
    @Expose
    @SerializedName("la")
    val latitude: Double,

    @Expose
    @SerializedName("lo")
    val longitude: Double,

    @Expose
    @SerializedName("tuid")
    val trackUid: String
)