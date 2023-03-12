package com.vljx.hawkspeed.models.track

import android.location.Location

sealed class RaceState {
    object Preparing: RaceState()
    data class CountdownStarted(
        val trackUid: String,
        val startedAt: Location
    ): RaceState()
    data class Racing(
        val trackUid: String,
        val countdownStartedAt: Location
    ): RaceState()
    object Cancelled: RaceState()
    data class Disqualified(
        val falseStart: Boolean = false
    ): RaceState()
    object Finished: RaceState()
}