package com.vljx.hawkspeed.data.socket.requestmodels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestViewportUpdateDto(
    @Expose
    @SerializedName("viewport")
    val viewport: RequestViewportDto
)