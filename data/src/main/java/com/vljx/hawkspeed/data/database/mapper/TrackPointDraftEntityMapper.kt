package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.track.TrackPointDraftEntity
import com.vljx.hawkspeed.data.models.track.TrackDraftWithPointsModel
import com.vljx.hawkspeed.data.models.track.TrackPointDraftModel
import javax.inject.Inject

class TrackPointDraftEntityMapper @Inject constructor(

): EntityMapper<TrackPointDraftEntity, TrackPointDraftModel> {
    override fun mapFromEntity(entity: TrackPointDraftEntity): TrackPointDraftModel {
        return TrackPointDraftModel(
            entity.trackPointDraftId!!,
            entity.latitude,
            entity.longitude,
            entity.loggedAt,
            entity.speed,
            entity.rotation,
            entity.trackDraftId
        )
    }

    override fun mapToEntity(model: TrackPointDraftModel): TrackPointDraftEntity {
        return TrackPointDraftEntity(
            model.trackPointDraftId,
            model.latitude,
            model.longitude,
            model.loggedAt,
            model.speed,
            model.rotation,
            model.trackDraftId
        )
    }
}