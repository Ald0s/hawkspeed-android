package com.vljx.hawkspeed.data.socket.mapper.race

import com.vljx.hawkspeed.data.models.race.StartRaceResultModel
import com.vljx.hawkspeed.data.socket.mapper.SocketDtoMapper
import com.vljx.hawkspeed.data.socket.models.StartRaceResponseDto
import javax.inject.Inject

class StartRaceResponseDtoMapper @Inject constructor(
    private val raceUpdateDtoMapper: RaceUpdateDtoMapper
): SocketDtoMapper<StartRaceResponseDto, StartRaceResultModel> {
    override suspend fun mapFromDto(dto: StartRaceResponseDto): StartRaceResultModel {
        return StartRaceResultModel(
            dto.isStarted,
            dto.race?.let { raceUpdateDtoMapper.mapFromDto(it) },
            dto.errorCode
        )
    }
}