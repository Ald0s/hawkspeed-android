package com.vljx.hawkspeed.data.mapper.track

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.mapper.comment.TrackCommentMapper
import com.vljx.hawkspeed.data.models.track.TrackCommentsPageModel
import com.vljx.hawkspeed.domain.models.trackcomment.TrackComments
import javax.inject.Inject

class TrackCommentsMapper @Inject constructor(
    private val trackMapper: TrackMapper,
    private val trackCommentMapper: TrackCommentMapper
): Mapper<TrackCommentsPageModel, TrackComments> {
    override fun mapFromData(model: TrackCommentsPageModel): TrackComments {
        TODO("Not yet implemented")
    }

    override fun mapToData(domain: TrackComments): TrackCommentsPageModel {
        TODO("Not yet implemented")
    }
}