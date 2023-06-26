package com.vljx.hawkspeed.domain.requestmodels.track

data class RequestSaveTrackPointDraft(
    val trackDraftId: Long,
    val latitude: Double,
    val longitude: Double,
    val loggedAt: Long,
    val speed: Float,
    val rotation: Float
)