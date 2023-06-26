package com.vljx.hawkspeed.data.socket.mapper.race

import com.vljx.hawkspeed.data.models.race.RaceDisqualifiedModel
import com.vljx.hawkspeed.data.socket.mapper.SocketDtoMapper
import com.vljx.hawkspeed.data.socket.models.race.RaceDisqualifiedDto
import javax.inject.Inject

class RaceDisqualifiedDtoMapper @Inject constructor(
    private val raceUpdateDtoMapper: RaceUpdateDtoMapper
): SocketDtoMapper<RaceDisqualifiedDto, RaceDisqualifiedModel> {
    override suspend fun mapFromDto(dto: RaceDisqualifiedDto): RaceDisqualifiedModel {
        return RaceDisqualifiedModel(
            raceUpdateDtoMapper.mapFromDto(dto.race)
        )
    }
}