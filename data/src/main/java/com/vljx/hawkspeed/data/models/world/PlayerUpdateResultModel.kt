package com.vljx.hawkspeed.data.models.world

data class PlayerUpdateResultModel(
    val latitude: Double,
    val longitude: Double,
    val rotation: Double,
    val worldObjectUpdateResult: WorldObjectUpdateResultModel?
)