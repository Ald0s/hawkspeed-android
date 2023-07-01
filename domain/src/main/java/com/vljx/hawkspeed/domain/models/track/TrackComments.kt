package com.vljx.hawkspeed.domain.models.track

data class TrackComments(
    val track: Track,
    val trackComments: List<TrackComment>
)