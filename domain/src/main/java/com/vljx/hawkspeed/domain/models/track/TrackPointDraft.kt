package com.vljx.hawkspeed.domain.models.track

data class TrackPointDraft(
    val trackPointDraftId: Long,
    val latitude: Double,
    val longitude: Double,
    val loggedAt: Long,
    val speed: Float,
    val rotation: Float,
    val trackDraftId: Long
)