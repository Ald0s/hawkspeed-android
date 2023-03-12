package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.TrackEntity
import com.vljx.hawkspeed.data.models.track.TrackModel
import javax.inject.Inject

class TrackEntityMapper @Inject constructor(
    private val userEntityMapper: UserEntityMapper,
    private val trackPointEntityMapper: TrackPointEntityMapper
): EntityMapper<TrackEntity, TrackModel> {
    override fun mapFromEntity(entity: TrackEntity): TrackModel {
        return TrackModel(
            entity.trackUid,
            entity.name,
            entity.description,
            userEntityMapper.mapFromEntity(entity.owner),
            trackPointEntityMapper.mapFromEntity(entity.startPoint),
            entity.isVerified,
            entity.canRace,
            entity.canEdit,
            entity.canDelete
        )
    }

    override fun mapToEntity(model: TrackModel): TrackEntity {
        return TrackEntity(
            model.trackUid,
            model.name,
            model.description,
            userEntityMapper.mapToEntity(model.owner),
            trackPointEntityMapper.mapToEntity(model.startPoint),
            model.isVerified,
            model.canRace,
            model.canEdit,
            model.canDelete
        )
    }
}