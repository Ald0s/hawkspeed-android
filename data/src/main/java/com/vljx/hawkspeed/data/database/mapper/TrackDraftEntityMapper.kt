package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.track.TrackDraftEntity
import com.vljx.hawkspeed.data.models.track.TrackDraftModel
import javax.inject.Inject

class TrackDraftEntityMapper @Inject constructor(

): EntityMapper<TrackDraftEntity, TrackDraftModel> {
    override fun mapFromEntity(entity: TrackDraftEntity): TrackDraftModel {
        return TrackDraftModel(
            entity.trackDraftId!!,
            entity.name,
            entity.description,
            entity.trackType
        )
    }

    override fun mapToEntity(model: TrackDraftModel): TrackDraftEntity {
        return TrackDraftEntity(
            model.trackDraftId,
            model.name,
            model.description,
            model.trackType,
        )
    }
}