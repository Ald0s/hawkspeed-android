package com.vljx.hawkspeed.domain.models.race

import android.os.Parcelable
import com.vljx.hawkspeed.domain.models.user.User
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RaceOutcome(
    val raceUid: String,
    val started: Long,
    val finished: Long,
    val stopwatch: Int,
    val player: User,
    val trackUid: String
): Parcelable