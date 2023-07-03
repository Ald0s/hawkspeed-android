package com.vljx.hawkspeed.data.socket.mapper.race

import com.vljx.hawkspeed.data.models.race.StartRaceResultModel
import com.vljx.hawkspeed.data.socket.mapper.SocketDtoMapper
import com.vljx.hawkspeed.data.socket.mapper.SocketErrorWrapperDtoMapper
import com.vljx.hawkspeed.data.socket.models.race.StartRaceResponseDto
import javax.inject.Inject

class StartRaceResponseDtoMapper @Inject constructor(
    private val raceUpdateDtoMapper: RaceUpdateDtoMapper,
    private val socketErrorWrapperDtoMapper: SocketErrorWrapperDtoMapper
): SocketDtoMapper<StartRaceResponseDto, StartRaceResultModel> {
    override suspend fun mapFromDto(dto: StartRaceResponseDto): StartRaceResultModel {
        return StartRaceResultModel(
            dto.isStarted,
            dto.race?.let { raceUpdateDtoMapper.mapFromDto(it) },
            dto.error?.let { socketErrorWrapperDtoMapper.mapFromDto(it) }
        )
    }
}