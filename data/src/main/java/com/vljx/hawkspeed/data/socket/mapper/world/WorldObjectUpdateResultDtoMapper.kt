package com.vljx.hawkspeed.data.socket.mapper.world

import com.vljx.hawkspeed.data.models.world.WorldObjectUpdateResultModel
import com.vljx.hawkspeed.data.network.mapper.track.TrackDtoMapper
import com.vljx.hawkspeed.data.socket.mapper.SocketDtoMapper
import com.vljx.hawkspeed.data.socket.models.WorldObjectUpdateResponseDto
import javax.inject.Inject

class WorldObjectUpdateResultDtoMapper @Inject constructor(
    private val trackDtoMapper: TrackDtoMapper
): SocketDtoMapper<WorldObjectUpdateResponseDto, WorldObjectUpdateResultModel> {
    override suspend fun mapFromDto(dto: WorldObjectUpdateResponseDto): WorldObjectUpdateResultModel {
        return WorldObjectUpdateResultModel(
            trackDtoMapper.mapFromDtoList(dto.tracks)
        )
    }
}