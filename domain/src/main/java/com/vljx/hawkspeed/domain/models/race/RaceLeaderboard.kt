package com.vljx.hawkspeed.domain.models.race

import android.os.Parcelable
import com.vljx.hawkspeed.domain.Extension.toRaceTime
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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
    /**
     * Converts stopwatch, which is a count of milliseconds for the duration of the race, to a format like: 13:23:421 for minutes, seconds and milliseconds.
     */
    @IgnoredOnParcel
    val prettyTime: String
        get() = stopwatch
            .toDuration(DurationUnit.MILLISECONDS)
            .toRaceTime()
}