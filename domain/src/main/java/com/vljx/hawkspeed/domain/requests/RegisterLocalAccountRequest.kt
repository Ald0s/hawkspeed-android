package com.vljx.hawkspeed.domain.requests

data class RegisterLocalAccountRequest(
    val emailAddress: String,
    val password: String,
    val confirmPassword: String
)