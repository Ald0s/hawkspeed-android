package com.vljx.hawkspeed.data.network.mapper

interface DtoMapper<Dto, Model> {
    fun mapFromDto(dto: Dto): Model
}