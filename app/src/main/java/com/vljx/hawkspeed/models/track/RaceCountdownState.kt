package com.vljx.hawkspeed.models.track

sealed class RaceCountdownState {
    object Idle: RaceCountdownState()
    object GetReady: RaceCountdownState()
    data class OnCount(
        val currentSecond: Int
    ): RaceCountdownState()
    object Go: RaceCountdownState()
}