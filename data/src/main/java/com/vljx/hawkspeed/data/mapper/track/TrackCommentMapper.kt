package com.vljx.hawkspeed.data.mapper.track

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.mapper.user.UserMapper
import com.vljx.hawkspeed.data.models.track.TrackCommentModel
import com.vljx.hawkspeed.domain.models.track.TrackComment
import javax.inject.Inject

class TrackCommentMapper @Inject constructor(
    private val userMapper: UserMapper
): Mapper<TrackCommentModel, TrackComment> {
    override fun mapFromData(model: TrackCommentModel): TrackComment {
        return TrackComment(
            model.commentUid,
            model.created,
            model.text,
            userMapper.mapFromData(model.user),
            model.trackUid
        )
    }

    override fun mapToData(domain: TrackComment): TrackCommentModel {
        return TrackCommentModel(
            domain.commentUid,
            domain.created,
            domain.text,
            userMapper.mapToData(domain.user),
            domain.trackUid
        )
    }
}