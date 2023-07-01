package com.vljx.hawkspeed.domain.models.world

data class Coordinate(
    val latitude: Double,
    val longitude: Double,
    val crs: Int = 4326
)