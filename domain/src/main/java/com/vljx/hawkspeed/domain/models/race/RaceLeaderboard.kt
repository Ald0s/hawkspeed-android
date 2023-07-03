package com.vljx.hawkspeed.domain.models.race

import android.os.Parcelable
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class RaceLeaderboard(
    val raceUid: String,
    val finishingPlace: Int,
    val started: Long,
    val finished: Long,
    val stopwatch: Int,
    val player: User,
    val vehicle: Vehicle,
    val trackUid: String
): Parcelable {
    // TODO: finish pretty time.
    @IgnoredOnParcel
    val prettyTime: String
        get() = "${stopwatch/1000L}.${stopwatch%1000L}s"
}