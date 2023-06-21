package com.vljx.hawkspeed.data.models.track

data class TrackPathWithPointsModel(
    val trackPathUid: String,
    val points: List<TrackPointModel>
)