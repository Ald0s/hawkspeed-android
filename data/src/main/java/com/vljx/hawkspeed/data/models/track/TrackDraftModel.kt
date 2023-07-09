package com.vljx.hawkspeed.data.models.track

import com.vljx.hawkspeed.domain.enums.TrackType

data class TrackDraftModel(
    var trackDraftId: Long?,
    val trackType: TrackType,
    val name: String?,
    val description: String?
)