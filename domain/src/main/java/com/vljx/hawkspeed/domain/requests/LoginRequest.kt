package com.vljx.hawkspeed.domain.requests

data class LoginRequest(
    val emailAddress: String,
    val password: String,
    val rememberMe: Boolean
)