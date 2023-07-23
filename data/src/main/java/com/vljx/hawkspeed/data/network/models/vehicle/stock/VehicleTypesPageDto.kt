package com.vljx.hawkspeed.data.network.models.vehicle.stock

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.base.Paged

data class VehicleTypesPageDto(
    @Expose
    @SerializedName("items")
    val types: List<VehicleTypeDto>,

    @Expose
    @SerializedName("this_page")
    override val thisPage: Int,

    @Expose
    @SerializedName("next_page")
    override val nextPage: Int?
): Paged