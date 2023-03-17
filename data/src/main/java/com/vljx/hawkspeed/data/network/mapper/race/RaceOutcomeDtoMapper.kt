package com.vljx.hawkspeed.data.network.mapper.race

import com.vljx.hawkspeed.data.models.race.RaceOutcomeModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.mapper.user.UserDtoMapper
import com.vljx.hawkspeed.data.network.models.race.RaceOutcomeDto
import javax.inject.Inject

class RaceOutcomeDtoMapper @Inject constructor(
    private val userDtoMapper: UserDtoMapper
): DtoMapper<RaceOutcomeDto, RaceOutcomeModel> {
    override fun mapFromDto(dto: RaceOutcomeDto): RaceOutcomeModel {
        return RaceOutcomeModel(
            dto.raceUid,
            dto.started,
            dto.finished,
            dto.stopwatch,
            userDtoMapper.mapFromDto(dto.player),
            dto.trackUid
        )
    }
}