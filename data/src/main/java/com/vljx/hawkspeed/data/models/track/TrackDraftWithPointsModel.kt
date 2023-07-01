package com.vljx.hawkspeed.data.models.track

data class TrackDraftWithPointsModel(
    val trackDraft: TrackDraftModel,
    val trackPoints: List<TrackPointDraftModel>
)