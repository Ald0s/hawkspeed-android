package com.vljx.hawkspeed.draft.track

import android.location.Location
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class RecordedPointDraft(
    val pointIndex: Int,
    val latitude: Double,
    val longitude: Double,
    val rotation: Float,
    val speed: Float,
    val loggedAt: Long
): Parcelable {
    constructor(pointIndex: Int, location: Location):
            this(
                pointIndex,
                location.latitude,
                location.longitude,
                location.bearing,
                location.speed,
                location.time
            )
}