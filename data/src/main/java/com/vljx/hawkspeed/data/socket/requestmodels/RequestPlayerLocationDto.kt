package com.vljx.hawkspeed.data.socket.requestmodels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate

data class RequestPlayerLocationDto(
    @Expose
    @SerializedName("latitude")
    val latitude: Double,

    @Expose
    @SerializedName("longitude")
    val longitude: Double,

    @Expose
    @SerializedName("bearing")
    val bearing: Float,

    @Expose
    @SerializedName("speed")
    val speed: Float,

    @Expose
    @SerializedName("logged_at")
    val loggedAt: Long
) {
    constructor(requestPlayerUpdate: RequestPlayerUpdate):
            this(
                requestPlayerUpdate.latitude,
                requestPlayerUpdate.longitude,
                requestPlayerUpdate.bearing,
                requestPlayerUpdate.speed,
                requestPlayerUpdate.loggedAt
            )
}