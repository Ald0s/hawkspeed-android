package com.vljx.hawkspeed.data.models.track

data class TrackPointDraftModel(
    var trackPointDraftId: Long,
    val latitude: Double,
    val longitude: Double,
    val loggedAt: Long,
    val speed: Float,
    val rotation: Float,
    val trackDraftId: Long
)