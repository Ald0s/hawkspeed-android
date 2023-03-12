package com.vljx.hawkspeed.domain.models.track

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val trackUid: String
): Parcelable