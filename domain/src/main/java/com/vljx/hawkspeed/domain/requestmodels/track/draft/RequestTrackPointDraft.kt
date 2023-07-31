package com.vljx.hawkspeed.domain.requestmodels.track.draft

import com.vljx.hawkspeed.domain.models.world.PlayerPosition

data class RequestTrackPointDraft(
    val latitude: Double,
    val longitude: Double,
    val loggedAt: Long,
    val speed: Float,
    val rotation: Float
) {
    constructor(playerPosition: PlayerPosition):
            this(
                playerPosition.latitude,
                playerPosition.longitude,
                playerPosition.loggedAt,
                playerPosition.speed,
                playerPosition.bearing
            )
}