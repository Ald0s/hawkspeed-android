package com.vljx.hawkspeed.domain.models.world

import android.location.Location

/**
 * HawkSpeed's abstract of Google's Location object.
 */
data class PlayerPosition(
    val latitude: Double,
    val longitude: Double,
    val rotation: Float,
    val speed: Float,
    val loggedAt: Long
) {
    constructor(location: Location):
            this(
                location.latitude,
                location.longitude,
                location.bearing,
                location.speed,
                location.time
            )
}