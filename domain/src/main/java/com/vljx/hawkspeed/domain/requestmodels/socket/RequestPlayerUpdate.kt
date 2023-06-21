package com.vljx.hawkspeed.domain.requestmodels.socket

import com.vljx.hawkspeed.domain.models.world.PlayerPosition

data class RequestPlayerUpdate(
    val latitude: Double,
    val longitude: Double,
    val rotation: Float,
    val speed: Float,
    val loggedAt: Long
) {
    constructor(playerPosition: PlayerPosition): this(
        playerPosition.latitude,
        playerPosition.longitude,
        playerPosition.rotation,
        playerPosition.speed,
        playerPosition.loggedAt
    )
}