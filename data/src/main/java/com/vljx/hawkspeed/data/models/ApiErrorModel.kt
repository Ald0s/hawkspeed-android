package com.vljx.hawkspeed.data.models

import com.vljx.hawkspeed.domain.base.BaseApiError

data class ApiErrorModel(
    override val severity: String,
    override val name: String,
    override val errorInformation: HashMap<String, String>
): BaseApiError