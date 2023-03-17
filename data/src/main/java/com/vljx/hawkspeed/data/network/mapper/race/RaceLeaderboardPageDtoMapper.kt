package com.vljx.hawkspeed.data.network.mapper.race

import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.network.mapper.track.TrackDtoMapper
import com.vljx.hawkspeed.data.network.models.race.RaceLeaderboardPageDto
import javax.inject.Inject

class RaceLeaderboardPageDtoMapper @Inject constructor(
    private val trackDtoMapper: TrackDtoMapper,
    private val raceOutcomeDtoMapper: RaceOutcomeDtoMapper
): DtoMapper<RaceLeaderboardPageDto, RaceLeaderboardPageModel> {
    override fun mapFromDto(dto: RaceLeaderboardPageDto): RaceLeaderboardPageModel {
        return RaceLeaderboardPageModel(
            trackDtoMapper.mapFromDto(dto.trackDto),
            dto.raceOutcomes.map { raceOutcomeDtoMapper.mapFromDto(it) },
            dto.thisPage,
            dto.nextPage
        )
    }
}