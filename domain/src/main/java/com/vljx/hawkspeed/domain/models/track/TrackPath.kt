package com.vljx.hawkspeed.domain.models.track

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrackPath(
    val trackPathUid: String,
    val points: List<TrackPoint>
): Parcelable