package com.vljx.hawkspeed.domain.requestmodels.track.draft

data class RequestTrackPointDraft(
    val latitude: Double,
    val longitude: Double,
    val loggedAt: Long,
    val speed: Float,
    val rotation: Float
)