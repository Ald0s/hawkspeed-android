package com.vljx.hawkspeed.data.network.requests.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.requests.SubmitTrackPointRequest
import java.util.*

data class SubmitTrackPointRequestDto(
    @Expose
    @SerializedName("latitude")
    val latitude: Double,

    @Expose
    @SerializedName("longitude")
    val longitude: Double,

    @Expose
    @SerializedName("logged_at")
    val loggedAt: Date,

    @Expose
    @SerializedName("speed")
    val speed: Float,

    @Expose
    @SerializedName("rotation")
    val rotation: Float
) {
    constructor(submitTrackPointRequest: SubmitTrackPointRequest):
            this(
                submitTrackPointRequest.latitude,
                submitTrackPointRequest.longitude,
                submitTrackPointRequest.loggedAt,
                submitTrackPointRequest.speed,
                submitTrackPointRequest.rotation
            )
}