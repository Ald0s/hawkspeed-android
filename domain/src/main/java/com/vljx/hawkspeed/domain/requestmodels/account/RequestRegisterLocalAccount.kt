package com.vljx.hawkspeed.domain.requestmodels.account

data class RequestRegisterLocalAccount(
    val emailAddress: String,
    val password: String,
    val confirmPassword: String
)