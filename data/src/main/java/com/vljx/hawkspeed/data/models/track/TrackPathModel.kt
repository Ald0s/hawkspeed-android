package com.vljx.hawkspeed.data.models.track

data class TrackPathModel(
    val trackUid: String,
    val hash: String,
    val points: List<TrackPointModel>
)