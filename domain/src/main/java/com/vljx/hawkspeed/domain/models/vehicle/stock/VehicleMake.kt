package com.vljx.hawkspeed.domain.models.vehicle.stock

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VehicleMake(
    val makeUid: String,
    val makeName: String
): Parcelable