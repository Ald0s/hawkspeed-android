package com.vljx.hawkspeed.data.mapper.track

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.track.TrackCommentsPageModel
import com.vljx.hawkspeed.domain.models.track.TrackComments
import javax.inject.Inject

class TrackCommentsMapper @Inject constructor(
    private val trackMapper: TrackMapper,
    private val trackCommentMapper: TrackCommentMapper
): Mapper<TrackCommentsPageModel, TrackComments> {
    override fun mapFromData(model: TrackCommentsPageModel): TrackComments {
        return TrackComments(
            trackMapper.mapFromData(model.track),
            trackCommentMapper.mapFromDataList(model.comments)
        )
    }

    override fun mapToData(domain: TrackComments): TrackCommentsPageModel {
        throw NotImplementedError("TrackCommentsMapper can't map track comments back to a page model - page information is lost!")
    }
}