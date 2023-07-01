package com.vljx.hawkspeed.data.mapper.track

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.track.TrackPointDraftModel
import com.vljx.hawkspeed.domain.models.track.TrackPointDraft
import javax.inject.Inject

class TrackPointDraftMapper @Inject constructor(

): Mapper<TrackPointDraftModel, TrackPointDraft> {
    override fun mapFromData(model: TrackPointDraftModel): TrackPointDraft {
        return TrackPointDraft(
            model.trackPointDraftId,
            model.latitude,
            model.longitude,
            model.loggedAt,
            model.speed,
            model.rotation,
            model.trackDraftId
        )
    }

    override fun mapToData(domain: TrackPointDraft): TrackPointDraftModel {
        return TrackPointDraftModel(
            domain.trackPointDraftId,
            domain.latitude,
            domain.longitude,
            domain.loggedAt,
            domain.speed,
            domain.rotation,
            domain.trackDraftId
        )
    }
}