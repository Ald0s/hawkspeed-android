package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.track.TrackPointEntity
import com.vljx.hawkspeed.data.models.track.TrackPointModel
import javax.inject.Inject

class TrackPointEntityMapper @Inject constructor(

): EntityMapper<TrackPointEntity, TrackPointModel> {
    override fun mapFromEntity(entity: TrackPointEntity): TrackPointModel {
        return TrackPointModel(
            entity.latitude,
            entity.longitude,
            entity.trackPathUid
        )
    }

    override fun mapToEntity(model: TrackPointModel): TrackPointEntity {
        return TrackPointEntity(
            null,
            model.latitude,
            model.longitude,
            model.trackUid
        )
    }
}