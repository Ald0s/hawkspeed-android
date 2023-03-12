package com.vljx.hawkspeed.domain.requests

data class SetupProfileRequest(
    val userName: String,
    val bio: String?
)