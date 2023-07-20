package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.track.TrackCommentEntity
import com.vljx.hawkspeed.data.models.track.TrackCommentModel
import javax.inject.Inject

class TrackCommentEntityMapper @Inject constructor(
    private val userEntityMapper: UserEntityMapper
): EntityMapper<TrackCommentEntity, TrackCommentModel> {
    override fun mapFromEntity(entity: TrackCommentEntity): TrackCommentModel {
        return TrackCommentModel(
            entity.commentUid,
            entity.created,
            entity.text,
            userEntityMapper.mapFromEntity(entity.user),
            entity.trackUid
        )
    }

    override fun mapToEntity(model: TrackCommentModel): TrackCommentEntity {
        return TrackCommentEntity(
            model.commentUid,
            model.created,
            model.text,
            userEntityMapper.mapToEntity(model.user),
            model.trackUid
        )
    }
}