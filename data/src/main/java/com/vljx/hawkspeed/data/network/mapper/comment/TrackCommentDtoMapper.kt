package com.vljx.hawkspeed.data.network.mapper.comment

import com.vljx.hawkspeed.data.models.comment.CommentModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.mapper.user.UserDtoMapper
import com.vljx.hawkspeed.data.network.models.comment.TrackCommentDto
import javax.inject.Inject

class TrackCommentDtoMapper @Inject constructor(
    private val userDtoMapper: UserDtoMapper
): DtoMapper<TrackCommentDto, CommentModel> {
    override fun mapFromDto(dto: TrackCommentDto): CommentModel {
        TODO("Not yet implemented")
    }
}