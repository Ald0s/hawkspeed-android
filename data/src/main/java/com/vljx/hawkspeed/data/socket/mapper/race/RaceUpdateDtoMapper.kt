package com.vljx.hawkspeed.data.socket.mapper.race

import com.vljx.hawkspeed.data.models.race.RaceUpdateModel
import com.vljx.hawkspeed.data.socket.mapper.SocketDtoMapper
import com.vljx.hawkspeed.data.socket.models.race.RaceUpdateDto
import javax.inject.Inject

class RaceUpdateDtoMapper @Inject constructor(

): SocketDtoMapper<RaceUpdateDto, RaceUpdateModel> {
    override suspend fun mapFromDto(dto: RaceUpdateDto): RaceUpdateModel {
        return RaceUpdateModel(
            dto.raceUid,
            dto.trackUid,
            dto.started,
            dto.finished,
            dto.isDisqualified,
            dto.disqualificationReason,
            dto.isCancelled
        )
    }
}