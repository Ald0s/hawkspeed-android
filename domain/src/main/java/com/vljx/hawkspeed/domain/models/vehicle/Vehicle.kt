package com.vljx.hawkspeed.domain.models.vehicle

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vehicle(
    val vehicleUid: String,
    val text: String,
    val belongsToYou: Boolean
): Parcelable {
    /**
     * The vehicle's title. Eventually, this should be a separate attribute, not a property.
     */
    val title: String
        get() = text
}