package com.vljx.hawkspeed.data.network.mapper.track

import com.vljx.hawkspeed.data.models.track.TrackPathModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.track.TrackPathDto
import javax.inject.Inject

class TrackPathDtoMapper @Inject constructor(
    private val trackPointDtoMapper: TrackPointDtoMapper
): DtoMapper<TrackPathDto, TrackPathModel> {
    override fun mapFromDto(dto: TrackPathDto): TrackPathModel {
        return TrackPathModel(
            dto.trackUid,
            trackPointDtoMapper.mapFromDtoList(dto.points)
        )
    }
}