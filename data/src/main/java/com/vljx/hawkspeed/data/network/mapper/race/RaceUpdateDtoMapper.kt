package com.vljx.hawkspeed.data.network.mapper.race

import com.vljx.hawkspeed.data.models.race.RaceModel
import com.vljx.hawkspeed.data.network.mapper.DtoMapper
import com.vljx.hawkspeed.data.socket.models.race.RaceUpdateDto
import javax.inject.Inject

class RaceUpdateDtoMapper @Inject constructor(

): DtoMapper<RaceUpdateDto, RaceModel> {
    override fun mapFromDto(dto: RaceUpdateDto): RaceModel {
        return RaceModel(
            dto.raceUid,
            dto.trackUid,
            dto.started,
            dto.finished,
            dto.isDisqualified,
            dto.disqualificationReason,
            dto.isCancelled,
            dto.averageSpeed,
            dto.numLapsComplete,
            dto.percentComplete
        )
    }
}