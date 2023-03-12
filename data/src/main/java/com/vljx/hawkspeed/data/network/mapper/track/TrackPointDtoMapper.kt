package com.vljx.hawkspeed.data.network.mapper.track

import com.vljx.hawkspeed.data.models.track.TrackPointModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.models.track.TrackPointDto
import javax.inject.Inject

class TrackPointDtoMapper @Inject constructor(

): DtoMapper<TrackPointDto, TrackPointModel> {
    override fun mapFromDto(dto: TrackPointDto): TrackPointModel {
        return TrackPointModel(
            dto.latitude,
            dto.longitude,
            dto.trackUid
        )
    }
}