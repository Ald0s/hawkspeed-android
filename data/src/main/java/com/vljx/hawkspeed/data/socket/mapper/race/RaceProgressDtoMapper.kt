package com.vljx.hawkspeed.data.socket.mapper.race

import com.vljx.hawkspeed.data.models.race.RaceProgressModel
import com.vljx.hawkspeed.data.socket.mapper.SocketDtoMapper
import com.vljx.hawkspeed.data.socket.models.race.RaceProgressDto
import javax.inject.Inject

class RaceProgressDtoMapper @Inject constructor(
    private val raceUpdateDtoMapper: RaceUpdateDtoMapper
): SocketDtoMapper<RaceProgressDto, RaceProgressModel> {
    override suspend fun mapFromDto(dto: RaceProgressDto): RaceProgressModel {
        return RaceProgressModel(
            raceUpdateDtoMapper.mapFromDto(dto.race)
        )
    }
}