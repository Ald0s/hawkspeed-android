package com.vljx.hawkspeed.data.models

import com.vljx.hawkspeed.domain.base.ApiErrorWrapper

data class ApiErrorWrapperModel(
    override val severity: String,
    override val name: String,
    override val errorInformation: HashMap<String, String>
): ApiErrorWrapper