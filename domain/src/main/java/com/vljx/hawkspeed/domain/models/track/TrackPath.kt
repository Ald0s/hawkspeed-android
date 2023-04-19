package com.vljx.hawkspeed.domain.models.track

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TrackPath(
    val trackUid: String,
    val points: List<TrackPoint>
): Parcelable