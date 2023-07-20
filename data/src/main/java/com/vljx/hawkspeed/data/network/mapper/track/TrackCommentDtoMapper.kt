package com.vljx.hawkspeed.data.network.mapper.track

import com.vljx.hawkspeed.data.models.track.TrackCommentModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.mapper.user.UserDtoMapper
import com.vljx.hawkspeed.data.network.models.track.TrackCommentDto
import javax.inject.Inject

class TrackCommentDtoMapper @Inject constructor(
    private val userDtoMapper: UserDtoMapper
): DtoMapper<TrackCommentDto, TrackCommentModel> {
    override fun mapFromDto(dto: TrackCommentDto): TrackCommentModel {
        return TrackCommentModel(
            dto.commentUid,
            dto.created,
            dto.text,
            userDtoMapper.mapFromDto(dto.user),
            dto.trackUid
        )
    }
}