package com.vljx.hawkspeed.data.network.mapper

import com.vljx.hawkspeed.data.models.ApiErrorWrapperModel
import com.vljx.hawkspeed.data.network.models.ApiErrorWrapperDto

class ApiErrorWrapperDtoMapper: DtoMapper<ApiErrorWrapperDto, ApiErrorWrapperModel> {
    override fun mapFromDto(dto: ApiErrorWrapperDto): ApiErrorWrapperModel {
        return ApiErrorWrapperModel(
            dto.severity,
            dto.name,
            dto.errorInformation
        )
    }
}