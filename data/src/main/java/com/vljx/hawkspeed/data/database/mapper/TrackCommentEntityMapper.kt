package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.track.TrackCommentEntity
import com.vljx.hawkspeed.data.models.comment.CommentModel
import javax.inject.Inject

class TrackCommentEntityMapper @Inject constructor(
    private val userEntityMapper: UserEntityMapper
): EntityMapper<TrackCommentEntity, CommentModel> {
    override fun mapFromEntity(entity: TrackCommentEntity): CommentModel {
        TODO("Not yet implemented")
    }

    override fun mapToEntity(model: CommentModel): TrackCommentEntity {
        TODO("Not yet implemented")
    }
}