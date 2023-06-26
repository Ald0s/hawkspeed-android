package com.vljx.hawkspeed.data.socket.mapper.world

import com.vljx.hawkspeed.data.models.world.PlayerUpdateResultModel
import com.vljx.hawkspeed.data.socket.mapper.SocketDtoMapper
import com.vljx.hawkspeed.data.socket.models.world.PlayerUpdateResponseDto
import javax.inject.Inject

class PlayerUpdateResultDtoMapper @Inject constructor(
    private val worldObjectUpdateResultDtoMapper: WorldObjectUpdateResultDtoMapper
): SocketDtoMapper<PlayerUpdateResponseDto, PlayerUpdateResultModel> {
    override suspend fun mapFromDto(dto: PlayerUpdateResponseDto): PlayerUpdateResultModel {
        return PlayerUpdateResultModel(
            dto.latitude,
            dto.longitude,
            dto.rotation,
            dto.worldObjectUpdate?.let { worldObjectUpdateResultDtoMapper.mapFromDto(it) }
        )
    }
}