package com.vljx.hawkspeed.domain.models.vehicle.stock

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VehicleModel(
    val modelUid: String,
    val modelName: String,
    val makeUid: String,
    val type: VehicleType
): Parcelable