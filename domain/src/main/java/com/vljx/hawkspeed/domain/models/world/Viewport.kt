package com.vljx.hawkspeed.domain.models.world

data class Viewport(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double,
    val zoom: Float
)