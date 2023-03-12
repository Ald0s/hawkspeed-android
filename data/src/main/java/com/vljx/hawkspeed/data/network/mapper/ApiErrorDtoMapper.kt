package com.vljx.hawkspeed.data.network.mapper

import com.vljx.hawkspeed.data.models.ApiErrorModel
import com.vljx.hawkspeed.data.network.models.ApiErrorDto

class ApiErrorDtoMapper: DtoMapper<ApiErrorDto, ApiErrorModel> {
    override fun mapFromDto(dto: ApiErrorDto): ApiErrorModel {
        return ApiErrorModel(
            dto.severity,
            dto.name,
            dto.errorInformation
        )
    }
}