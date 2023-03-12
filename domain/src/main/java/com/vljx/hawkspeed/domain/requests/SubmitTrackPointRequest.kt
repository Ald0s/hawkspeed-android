package com.vljx.hawkspeed.domain.requests

import java.util.*

data class SubmitTrackPointRequest(
    val latitude: Double,
    val longitude: Double,
    val loggedAt: Date,
    val speed: Float,
    val rotation: Float
)