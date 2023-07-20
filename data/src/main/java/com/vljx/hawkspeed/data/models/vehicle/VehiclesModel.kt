package com.vljx.hawkspeed.data.models.vehicle

import com.vljx.hawkspeed.data.models.user.UserModel

data class VehiclesModel(
    val user: UserModel,
    val vehicles: List<VehicleModel>
)