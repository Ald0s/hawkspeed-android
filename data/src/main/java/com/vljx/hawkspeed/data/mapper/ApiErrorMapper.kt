package com.vljx.hawkspeed.data.mapper

import com.vljx.hawkspeed.data.models.ApiErrorModel
import com.vljx.hawkspeed.domain.models.ApiError
import javax.inject.Inject

class ApiErrorMapper @Inject constructor(

): Mapper<ApiErrorModel, ApiError> {
    override fun mapFromData(model: ApiErrorModel): ApiError {
        return ApiError(
            model.severity,
            model.name,
            model.errorInformation
        )
    }

    override fun mapToData(domain: ApiError): ApiErrorModel {
        return ApiErrorModel(
            domain.severity,
            domain.name,
            domain.errorInformation
        )
    }
}