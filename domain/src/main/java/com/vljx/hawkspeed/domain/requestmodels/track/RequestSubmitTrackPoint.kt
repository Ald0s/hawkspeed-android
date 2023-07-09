package com.vljx.hawkspeed.domain.requestmodels.track

import com.vljx.hawkspeed.domain.models.track.TrackPointDraft

data class RequestSubmitTrackPoint(
    val latitude: Double,
    val longitude: Double,
    val loggedAt: Long,
    val speed: Float,
    val rotation: Float
) {
    constructor(trackPointDraft: TrackPointDraft):
            this(
                trackPointDraft.latitude,
                trackPointDraft.longitude,
                trackPointDraft.loggedAt,
                trackPointDraft.speed,
                trackPointDraft.rotation
            )
}