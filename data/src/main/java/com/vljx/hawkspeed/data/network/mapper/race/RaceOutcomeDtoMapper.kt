package com.vljx.hawkspeed.data.network.mapper.race

import com.vljx.hawkspeed.data.models.race.RaceLeaderboardModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.mapper.user.UserDtoMapper
import com.vljx.hawkspeed.data.network.models.race.RaceOutcomeDto
import javax.inject.Inject

class RaceOutcomeDtoMapper @Inject constructor(
    private val userDtoMapper: UserDtoMapper
): DtoMapper<RaceOutcomeDto, RaceLeaderboardModel> {
    override fun mapFromDto(dto: RaceOutcomeDto): RaceLeaderboardModel {
        return RaceLeaderboardModel(
            dto.raceUid,
            dto.finishingPlace,
            dto.started,
            dto.finished,
            dto.stopwatch,
            userDtoMapper.mapFromDto(dto.player),
            dto.trackUid
        )
    }
}