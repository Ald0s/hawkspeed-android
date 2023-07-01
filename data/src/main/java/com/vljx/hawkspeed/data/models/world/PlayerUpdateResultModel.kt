package com.vljx.hawkspeed.data.models.world

data class PlayerUpdateResultModel(
    val latitude: Double,
    val longitude: Double,
    val rotation: Float,
    val worldObjectUpdateResult: WorldObjectUpdateResultModel?
)