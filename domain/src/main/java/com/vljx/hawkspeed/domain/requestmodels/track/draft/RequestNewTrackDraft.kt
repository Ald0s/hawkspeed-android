package com.vljx.hawkspeed.domain.requestmodels.track.draft

import com.vljx.hawkspeed.domain.enums.TrackType

data class RequestNewTrackDraft(
    val trackType: TrackType
)