package com.vljx.hawkspeed.domain.requestmodels.track

import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints

data class RequestSubmitTrack(
    val name: String,
    val description: String,
    val trackType: TrackType,
    val points: List<RequestSubmitTrackPoint>
) {
    constructor(
        name: String,
        description: String,
        trackDraftWithPoints: TrackDraftWithPoints
    ): this(
        name,
        description,
        trackDraftWithPoints.trackType,
        trackDraftWithPoints.pointDrafts.map { pointDraft ->
            RequestSubmitTrackPoint(
                pointDraft
            )
        }
    )
}