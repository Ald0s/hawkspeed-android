package com.vljx.hawkspeed.domain.requestmodels.track

data class RequestSubmitTrack(
    val name: String,
    val description: String,
    val points: List<RequestSubmitTrackPoint>
)