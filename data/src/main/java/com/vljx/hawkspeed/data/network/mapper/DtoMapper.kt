package com.vljx.hawkspeed.data.network.mapper

interface DtoMapper<Dto, Model> {
    fun mapFromDto(dto: Dto): Model
    fun mapFromDtoList(dtos: List<Dto>): List<Model> =
        dtos.map { dto ->
            mapFromDto(dto)
        }
}