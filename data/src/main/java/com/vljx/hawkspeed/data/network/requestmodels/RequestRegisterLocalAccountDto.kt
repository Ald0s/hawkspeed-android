package com.vljx.hawkspeed.data.network.requestmodels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.requestmodels.account.RequestRegisterLocalAccount
import java.util.*

data class RequestRegisterLocalAccountDto(
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
    constructor(requestRegisterLocalAccount: RequestRegisterLocalAccount):
            this(
                requestRegisterLocalAccount.emailAddress,
                requestRegisterLocalAccount.password,
                requestRegisterLocalAccount.confirmPassword,
            )
}