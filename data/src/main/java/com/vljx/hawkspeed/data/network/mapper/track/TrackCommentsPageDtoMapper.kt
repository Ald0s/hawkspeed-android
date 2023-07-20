package com.vljx.hawkspeed.data.network.mapper.track

import com.vljx.hawkspeed.data.models.track.TrackCommentsPageModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.track.TrackCommentsPageDto
import javax.inject.Inject

class TrackCommentsPageDtoMapper @Inject constructor(
    private val trackDtoMapper: TrackDtoMapper,
    private val trackCommentDtoMapper: TrackCommentDtoMapper
): DtoMapper<TrackCommentsPageDto, TrackCommentsPageModel> {
    override fun mapFromDto(dto: TrackCommentsPageDto): TrackCommentsPageModel {
        return TrackCommentsPageModel(
            trackDtoMapper.mapFromDto(dto.track),
            trackCommentDtoMapper.mapFromDtoList(dto.comments),
            dto.thisPage,
            dto.nextPage
        )
    }
}