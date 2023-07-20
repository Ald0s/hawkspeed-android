package com.vljx.hawkspeed.data.network.models.vehicle

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.user.UserDto

data class VehiclesDto(
    @Expose
    @SerializedName("user")
    val user: UserDto,

    @Expose
    @SerializedName("items")
    val vehicles: List<VehicleDto>
)