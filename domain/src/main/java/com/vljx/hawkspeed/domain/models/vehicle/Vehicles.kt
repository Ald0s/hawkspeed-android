package com.vljx.hawkspeed.domain.models.vehicle

import com.vljx.hawkspeed.domain.models.user.User

data class Vehicles(
    val user: User,
    val vehicles: List<Vehicle>
)