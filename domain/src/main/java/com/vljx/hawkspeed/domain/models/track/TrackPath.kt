package com.vljx.hawkspeed.domain.models.track

data class TrackPath(
    val trackUid: String,
    val points: List<TrackPoint>
)