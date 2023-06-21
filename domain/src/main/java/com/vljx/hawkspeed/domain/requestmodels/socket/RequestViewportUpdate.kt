package com.vljx.hawkspeed.domain.requestmodels.socket

data class RequestViewportUpdate(
    val viewportMinX: Double,
    val viewportMinY: Double,
    val viewportMaxX: Double,
    val viewportMaxY: Double,
    val zoom: Float
)