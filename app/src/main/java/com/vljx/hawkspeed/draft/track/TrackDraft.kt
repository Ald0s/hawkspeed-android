package com.vljx.hawkspeed.draft.track

data class TrackDraft(
    // TODO: images
    // TODO: categories/warnings

    val name: String,
    val description: String,
    val points: List<RecordedPointDraft>
)