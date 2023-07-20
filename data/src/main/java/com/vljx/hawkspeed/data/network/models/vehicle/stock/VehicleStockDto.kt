package com.vljx.hawkspeed.data.network.models.vehicle.stock

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VehicleStockDto(
    @Expose
    @SerializedName("vehicle_uid")
    val vehicleStockUid: String,

    @Expose
    @SerializedName("make")
    val make: VehicleMakeDto,

    @Expose
    @SerializedName("model")
    val model: VehicleModelDto,

    @Expose
    @SerializedName("year")
    val year: Int,

    @Expose
    @SerializedName("version")
    val version: String?,

    @Expose
    @SerializedName("badge")
    val badge: String?,

    @Expose
    @SerializedName("motor_type")
    val motorType: String,

    @Expose
    @SerializedName("displacement")
    val displacement: Float,

    @Expose
    @SerializedName("induction")
    val induction: String?,

    @Expose
    @SerializedName("fuel_type")
    val fuelType: String?,

    @Expose
    @SerializedName("power")
    val power: Int?,

    @Expose
    @SerializedName("elec_type")
    val electricType: String?,

    @Expose
    @SerializedName("trans_type")
    val transmissionType: String?,

    @Expose
    @SerializedName("num_gears")
    val numGears: Int?
)