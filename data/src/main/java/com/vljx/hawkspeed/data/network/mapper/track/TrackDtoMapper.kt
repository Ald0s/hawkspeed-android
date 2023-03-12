package com.vljx.hawkspeed.data.network.mapper.track

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.mapper.user.UserDtoMapper
import com.vljx.hawkspeed.data.network.models.track.TrackDto
import javax.inject.Inject

class TrackDtoMapper @Inject constructor(
    private val trackPointDtoMapper: TrackPointDtoMapper,
    private val userDtoMapper: UserDtoMapper
): DtoMapper<TrackDto, TrackModel> {
    override fun mapFromDto(dto: TrackDto): TrackModel {
        return TrackModel(
            dto.trackUid,
            dto.name,
            dto.description,
            userDtoMapper.mapFromDto(dto.owner),
            trackPointDtoMapper.mapFromDto(dto.startPoint),
            dto.isVerified,
            dto.canRace,
            dto.canEdit,
            dto.canDelete,
            //dto.points.map { trackPointDtoMapper.mapFromDto(it) }
        )
    }
}