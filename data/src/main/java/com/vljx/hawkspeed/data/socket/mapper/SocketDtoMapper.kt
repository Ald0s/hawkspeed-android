package com.vljx.hawkspeed.data.socket.mapper

interface SocketDtoMapper<Dto, Model> {
    suspend fun mapFromDto(dto: Dto): Model
    suspend fun mapFromDtoList(dtos: List<Dto>): List<Model> =
        dtos.map { dto ->
            mapFromDto(dto)
        }
}