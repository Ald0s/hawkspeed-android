package com.vljx.hawkspeed.domain.requestmodels.track

import com.vljx.hawkspeed.domain.enums.TrackType

data class RequestSubmitTrack(
    val name: String,
    val description: String,
    val trackType: TrackType,
    val points: List<RequestSubmitTrackPoint>
)