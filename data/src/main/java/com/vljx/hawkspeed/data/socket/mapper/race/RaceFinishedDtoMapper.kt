package com.vljx.hawkspeed.data.socket.mapper.race

import com.vljx.hawkspeed.data.models.race.RaceFinishedModel
import com.vljx.hawkspeed.data.socket.mapper.SocketDtoMapper
import com.vljx.hawkspeed.data.socket.models.race.RaceFinishedDto
import javax.inject.Inject

class RaceFinishedDtoMapper @Inject constructor(
    private val raceUpdateDtoMapper: RaceUpdateDtoMapper
): SocketDtoMapper<RaceFinishedDto, RaceFinishedModel> {
    override suspend fun mapFromDto(dto: RaceFinishedDto): RaceFinishedModel {
        return RaceFinishedModel(
            raceUpdateDtoMapper.mapFromDto(dto.race)
        )
    }
}