package com.vljx.hawkspeed.draft.track

import android.location.Location
import java.util.Date

data class RecordedPointDraft(
    val pointIndex: Int,
    val latitude: Double,
    val longitude: Double,
    val rotation: Float,
    val speed: Float,
    val loggedAt: Long
) {
    constructor(pointIndex: Int, location: Location):
            this(
                pointIndex,
                location.latitude,
                location.longitude,
                location.bearing,
                location.speed,
                location.time
            )
}