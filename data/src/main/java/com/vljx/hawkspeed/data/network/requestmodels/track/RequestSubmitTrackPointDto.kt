package com.vljx.hawkspeed.data.network.requestmodels.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrackPoint
import java.util.*

data class RequestSubmitTrackPointDto(
    @Expose
    @SerializedName("latitude")
    val latitude: Double,

    @Expose
    @SerializedName("longitude")
    val longitude: Double,

    @Expose
    @SerializedName("logged_at")
    val loggedAt: Long,

    @Expose
    @SerializedName("speed")
    val speed: Float,

    @Expose
    @SerializedName("rotation")
    val rotation: Float
) {
    constructor(requestSubmitTrackPoint: RequestSubmitTrackPoint):
            this(
                requestSubmitTrackPoint.latitude,
                requestSubmitTrackPoint.longitude,
                requestSubmitTrackPoint.loggedAt,
                requestSubmitTrackPoint.speed,
                requestSubmitTrackPoint.rotation
            )
}