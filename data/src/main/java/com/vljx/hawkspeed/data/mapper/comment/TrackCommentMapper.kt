package com.vljx.hawkspeed.data.mapper.comment

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.mapper.user.UserMapper
import com.vljx.hawkspeed.data.models.comment.CommentModel
import com.vljx.hawkspeed.domain.models.comment.Comment
import javax.inject.Inject

class TrackCommentMapper @Inject constructor(
    private val userMapper: UserMapper
): Mapper<CommentModel, Comment> {
    override fun mapFromData(model: CommentModel): Comment {
        TODO("Not yet implemented")
    }

    override fun mapToData(domain: Comment): CommentModel {
        TODO("Not yet implemented")
    }
}