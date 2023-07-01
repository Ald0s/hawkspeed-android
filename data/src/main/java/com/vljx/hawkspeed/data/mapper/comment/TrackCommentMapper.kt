package com.vljx.hawkspeed.data.mapper.comment

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.mapper.user.UserMapper
import com.vljx.hawkspeed.data.models.comment.CommentModel
import com.vljx.hawkspeed.domain.models.track.TrackComment
import javax.inject.Inject

class TrackCommentMapper @Inject constructor(
    private val userMapper: UserMapper
): Mapper<CommentModel, TrackComment> {
    override fun mapFromData(model: CommentModel): TrackComment {
        TODO("Not yet implemented")
    }

    override fun mapToData(domain: TrackComment): CommentModel {
        TODO("Not yet implemented")
    }
}