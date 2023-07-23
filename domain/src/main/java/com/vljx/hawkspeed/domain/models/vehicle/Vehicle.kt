package com.vljx.hawkspeed.domain.models.vehicle

import android.os.Parcelable
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleStock
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vehicle(
    val vehicleUid: String,
    val title: String,
    val vehicleStock: VehicleStock,
    val user: User
): Parcelable