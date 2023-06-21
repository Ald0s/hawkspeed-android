package com.vljx.hawkspeed.domain.requestmodels.account

data class RequestLogin(
    val emailAddress: String,
    val password: String,
    val rememberMe: Boolean
)