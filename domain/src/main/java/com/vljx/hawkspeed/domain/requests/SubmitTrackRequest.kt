package com.vljx.hawkspeed.domain.requests

data class SubmitTrackRequest(
    val name: String,
    val description: String,
    val points: List<SubmitTrackPointRequest>
)