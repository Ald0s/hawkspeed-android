package com.vljx.hawkspeed.data.socket.requestmodels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestConnectAuthenticationDto(
    @Expose
    @SerializedName("device_fid")
    val deviceIdentifier: String,

    @Expose
    @SerializedName("latitude")
    val latitude: Double,

    @Expose
    @SerializedName("longitude")
    val longitude: Double,

    @Expose
    @SerializedName("rotation")
    val rotation: Float,

    @Expose
    @SerializedName("speed")
    val speed: Float,

    @Expose
    @SerializedName("logged_at")
    val loggedAt: Long
)