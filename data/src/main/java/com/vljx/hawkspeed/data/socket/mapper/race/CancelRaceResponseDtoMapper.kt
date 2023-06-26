package com.vljx.hawkspeed.data.socket.mapper.race

import com.vljx.hawkspeed.data.models.race.CancelRaceResultModel
import com.vljx.hawkspeed.data.socket.mapper.SocketDtoMapper
import com.vljx.hawkspeed.data.socket.models.race.CancelRaceResponseDto
import javax.inject.Inject

class CancelRaceResponseDtoMapper @Inject constructor(
    private val raceUpdateDtoMapper: RaceUpdateDtoMapper
): SocketDtoMapper<CancelRaceResponseDto, CancelRaceResultModel> {
    override suspend fun mapFromDto(dto: CancelRaceResponseDto): CancelRaceResultModel {
        return CancelRaceResultModel(
            dto.race?.let { raceUpdateDtoMapper.mapFromDto(it) },
            dto.reasonCode
        )
    }
}