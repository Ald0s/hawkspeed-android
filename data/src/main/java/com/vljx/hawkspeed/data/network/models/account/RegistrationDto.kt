package com.vljx.hawkspeed.data.network.models.account

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RegistrationDto(
    @Expose
    @SerializedName("email_address")
    val emailAddress: String
)