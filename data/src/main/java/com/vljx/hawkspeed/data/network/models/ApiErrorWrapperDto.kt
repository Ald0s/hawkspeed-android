package com.vljx.hawkspeed.data.network.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.base.ApiErrorWrapper

data class ApiErrorWrapperDto(
    @Expose
    @SerializedName("severity")
    override val severity: String,

    @Expose
    @SerializedName("name")
    override val name: String,

    @Expose
    @SerializedName("error")
    override val errorInformation: HashMap<String, String>
): ApiErrorWrapper