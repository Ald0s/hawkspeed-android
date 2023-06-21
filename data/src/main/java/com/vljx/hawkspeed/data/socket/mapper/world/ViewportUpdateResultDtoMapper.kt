package com.vljx.hawkspeed.data.socket.mapper.world

import com.vljx.hawkspeed.data.models.world.ViewportUpdateResultModel
import com.vljx.hawkspeed.data.network.mapper.track.TrackDtoMapper
import com.vljx.hawkspeed.data.socket.mapper.SocketDtoMapper
import com.vljx.hawkspeed.data.socket.models.ViewportUpdateResponseDto
import javax.inject.Inject

class ViewportUpdateResultDtoMapper @Inject constructor(
    private val trackDtoMapper: TrackDtoMapper
): SocketDtoMapper<ViewportUpdateResponseDto, ViewportUpdateResultModel> {
    override suspend fun mapFromDto(dto: ViewportUpdateResponseDto): ViewportUpdateResultModel {
        return ViewportUpdateResultModel(
            trackDtoMapper.mapFromDtoList(dto.tracks)
        )
    }
}