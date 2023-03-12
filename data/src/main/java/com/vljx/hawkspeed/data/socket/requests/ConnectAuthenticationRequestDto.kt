package com.vljx.hawkspeed.data.socket.requests

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ConnectAuthenticationRequestDto(
    @Expose
    @SerializedName("viewport_minx")
    val viewportMinX: Double,

    @Expose
    @SerializedName("viewport_miny")
    val viewportMinY: Double,

    @Expose
    @SerializedName("viewport_maxx")
    val viewportMaxX: Double,

    @Expose
    @SerializedName("viewport_maxy")
    val viewportMaxY: Double,

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
    val loggedAt: Int
)