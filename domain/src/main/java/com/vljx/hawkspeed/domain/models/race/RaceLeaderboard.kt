package com.vljx.hawkspeed.domain.models.race

import android.annotation.SuppressLint
import android.os.Parcelable
import com.vljx.hawkspeed.domain.Extension.toRaceTime
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Parcelize
data class RaceLeaderboard(
    val raceUid: String,
    val finishingPlace: Int,
    val started: Long,
    val finished: Long,
    val stopwatch: Int,
    val averageSpeed: Int?,
    val percentMissed: Int,
    val player: User,
    val vehicle: Vehicle,
    val trackUid: String
): Parcelable {
    /**
     * Converts stopwatch, which is a count of milliseconds for the duration of the race, to a format like: 13:23:421 for minutes, seconds and milliseconds.
     */
    @IgnoredOnParcel
    val raceTime: String
        get() = stopwatch
            .toDuration(DurationUnit.MILLISECONDS)
            .toRaceTime()

    /**
     * Returns the finished timestamp, which is in milliseconds, as a pretty date time capable of being displayed. The target format is;
     * 9:34pm on 07/06/2023
     */
    @IgnoredOnParcel
    val dateTimeAwarded: String
        @SuppressLint("SimpleDateFormat")
        get() = SimpleDateFormat("hh:mm a 'on' dd/MM/yyyy").let { simpleDateFormat ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = finished
            return simpleDateFormat.format(calendar.time)
        }
}