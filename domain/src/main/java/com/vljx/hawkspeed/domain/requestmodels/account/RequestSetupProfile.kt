package com.vljx.hawkspeed.domain.requestmodels.account

import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestCreateVehicle

data class RequestSetupProfile(
    val userName: String,
    val requestCreateVehicle: RequestCreateVehicle,
    val bio: String?
)