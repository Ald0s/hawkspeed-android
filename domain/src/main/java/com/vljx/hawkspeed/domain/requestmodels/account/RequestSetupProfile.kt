package com.vljx.hawkspeed.domain.requestmodels.account

data class RequestSetupProfile(
    val userName: String,
    val vehicleInformation: String,
    val bio: String?
)