package com.vljx.hawkspeed.domain.models.world

data class PlayerUpdateResult(
    val latitude: Double,
    val longitude: Double,
    val bearing: Float,
    val worldObjectUpdateResult: WorldObjectUpdateResult?
)