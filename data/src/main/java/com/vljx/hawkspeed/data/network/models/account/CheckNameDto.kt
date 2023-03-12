package com.vljx.hawkspeed.data.network.models.account

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CheckNameDto(
    @Expose
    @SerializedName("username")
    val userName: String,

    @Expose
    @SerializedName("is_taken")
    val isTaken: Boolean
)