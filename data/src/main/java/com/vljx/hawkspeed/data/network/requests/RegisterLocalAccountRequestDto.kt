package com.vljx.hawkspeed.data.network.requests

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.requests.RegisterLocalAccountRequest
import java.util.*

data class RegisterLocalAccountRequestDto(
    @Expose
    @SerializedName("email_address")
    val emailAddress: String,

    @Expose
    @SerializedName("password")
    val password: String,

    @Expose
    @SerializedName("confirm_password")
    val confirmPassword: String
) {
    constructor(registerLocalAccountRequest: RegisterLocalAccountRequest):
            this(
                registerLocalAccountRequest.emailAddress,
                registerLocalAccountRequest.password,
                registerLocalAccountRequest.confirmPassword,
            )
}