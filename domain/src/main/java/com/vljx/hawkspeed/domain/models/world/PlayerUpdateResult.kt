package com.vljx.hawkspeed.domain.models.world

data class PlayerUpdateResult(
    val latitude: Double,
    val longitude: Double,
    val rotation: Double,
    val worldObjectUpdateResult: WorldObjectUpdateResult?
)