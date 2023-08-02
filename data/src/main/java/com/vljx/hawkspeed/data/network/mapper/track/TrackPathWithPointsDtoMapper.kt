package com.vljx.hawkspeed.data.network.mapper.track

import com.vljx.hawkspeed.data.models.track.TrackPathWithPointsModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.track.TrackPathDto
import javax.inject.Inject

class TrackPathWithPointsDtoMapper @Inject constructor(
    private val trackPointDtoMapper: TrackPointDtoMapper
): DtoMapper<TrackPathDto, TrackPathWithPointsModel> {
    override fun mapFromDto(dto: TrackPathDto): TrackPathWithPointsModel {
        return TrackPathWithPointsModel(
            dto.trackUid,
            dto.hash,
            trackPointDtoMapper.mapFromDtoList(dto.points)
        )
    }
}