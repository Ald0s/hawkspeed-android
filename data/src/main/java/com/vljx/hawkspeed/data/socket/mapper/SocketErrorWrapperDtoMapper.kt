package com.vljx.hawkspeed.data.socket.mapper

import com.vljx.hawkspeed.data.models.SocketErrorWrapperModel
import com.vljx.hawkspeed.data.socket.models.SocketErrorWrapperDto
import javax.inject.Inject

class SocketErrorWrapperDtoMapper @Inject constructor(

): SocketDtoMapper<SocketErrorWrapperDto, SocketErrorWrapperModel> {
    override suspend fun mapFromDto(dto: SocketErrorWrapperDto): SocketErrorWrapperModel {
        return SocketErrorWrapperModel(
            dto.severity,
            dto.name,
            dto.errorInformation
        )
    }
}