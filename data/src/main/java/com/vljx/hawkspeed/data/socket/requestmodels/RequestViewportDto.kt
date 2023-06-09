package com.vljx.hawkspeed.data.socket.requestmodels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestViewportDto(
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
    @SerializedName("zoom")
    val zoom: Float
)