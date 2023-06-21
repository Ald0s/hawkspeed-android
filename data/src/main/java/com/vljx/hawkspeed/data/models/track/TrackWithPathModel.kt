package com.vljx.hawkspeed.data.models.track

data class TrackWithPathModel(
    val track: TrackModel,
    val trackPathWithPoints: TrackPathWithPointsModel?
)