package com.vljx.hawkspeed.data.network.mapper.race

import com.vljx.hawkspeed.data.models.race.RaceLeaderboardModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.mapper.user.UserDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.VehicleDtoMapper
import com.vljx.hawkspeed.data.network.models.race.RaceLeaderboardDto
import javax.inject.Inject

class RaceLeaderboardDtoMapper @Inject constructor(
    private val vehicleDtoMapper: VehicleDtoMapper,
    private val userDtoMapper: UserDtoMapper
): DtoMapper<RaceLeaderboardDto, RaceLeaderboardModel> {
    override fun mapFromDto(dto: RaceLeaderboardDto): RaceLeaderboardModel {
        return RaceLeaderboardModel(
            dto.raceUid,
            dto.finishingPlace,
            dto.started,
            dto.finished,
            dto.stopwatch,
            userDtoMapper.mapFromDto(dto.player),
            vehicleDtoMapper.mapFromDto(dto.vehicle),
            dto.trackUid
        )
    }
}