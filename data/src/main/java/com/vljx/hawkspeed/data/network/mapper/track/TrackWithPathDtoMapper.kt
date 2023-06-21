package com.vljx.hawkspeed.data.network.mapper.track

import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.track.TrackWithPathDto
import javax.inject.Inject

class TrackWithPathDtoMapper @Inject constructor(
    private val trackDtoMapper: TrackDtoMapper,
    private val trackPathWithPointsDtoMapper: TrackPathWithPointsDtoMapper
): DtoMapper<TrackWithPathDto, TrackWithPathModel> {
    override fun mapFromDto(dto: TrackWithPathDto): TrackWithPathModel {
        return TrackWithPathModel(
            trackDtoMapper.mapFromDto(dto.track),
            trackPathWithPointsDtoMapper.mapFromDto(dto.trackPath)
        )
    }
}