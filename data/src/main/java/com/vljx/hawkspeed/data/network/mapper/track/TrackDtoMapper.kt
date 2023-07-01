package com.vljx.hawkspeed.data.network.mapper.track

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.mapper.race.RaceOutcomeDtoMapper
import com.vljx.hawkspeed.data.network.mapper.user.UserDtoMapper
import com.vljx.hawkspeed.data.network.models.track.TrackDto
import javax.inject.Inject

class TrackDtoMapper @Inject constructor(
    private val trackPointDtoMapper: TrackPointDtoMapper,
    private val raceOutcomeDtoMapper: RaceOutcomeDtoMapper,
    private val userDtoMapper: UserDtoMapper
): DtoMapper<TrackDto, TrackModel> {
    override fun mapFromDto(dto: TrackDto): TrackModel {
        return TrackModel(
            dto.trackUid,
            dto.name,
            dto.description,
            userDtoMapper.mapFromDto(dto.owner),
            raceOutcomeDtoMapper.mapFromDtoList(dto.topLeaderboard),
            trackPointDtoMapper.mapFromDto(dto.startPoint),
            dto.isVerified,
            dto.trackType,
            dto.ratings.numPositiveVotes,
            dto.ratings.numNegativeVotes,
            dto.yourRating,
            dto.numComments,
            dto.canRace,
            dto.canEdit,
            dto.canDelete,
            dto.canComment
        )
    }
}