package com.vljx.hawkspeed.domain.models.vehicle.stock

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VehicleType(
    val typeId: String,
    val typeName: String,
    val description: String
): Parcelable