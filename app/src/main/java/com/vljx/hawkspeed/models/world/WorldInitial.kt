package com.vljx.hawkspeed.models.world

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WorldInitial(
    val playerUid: String,
    val latitude: Double,
    val longitude: Double,
    val rotation: Float
): Parcelable {
    companion object {
        const val ARG_WORLD_INITIAL = "com.vljx.hawkspeed.models.world.WorldInitial.ARG_WORLD_INITIAL"
    }
}