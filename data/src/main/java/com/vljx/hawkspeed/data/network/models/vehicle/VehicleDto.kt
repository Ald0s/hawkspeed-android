package com.vljx.hawkspeed.data.network.models.vehicle

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.user.UserDto
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleStockDto

data class VehicleDto(
    @Expose
    @SerializedName("uid")
    val vehicleUid: String,

    @Expose
    @SerializedName("title")
    val title: String,

    @Expose
    @SerializedName("stock")
    val vehicleStock: VehicleStockDto,

    @Expose
    @SerializedName("user")
    val user: UserDto
)