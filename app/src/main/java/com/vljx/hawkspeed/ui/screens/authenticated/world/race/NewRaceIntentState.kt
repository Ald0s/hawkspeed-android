package com.vljx.hawkspeed.ui.screens.authenticated.world.race

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.world.PlayerPosition

sealed class NewRaceIntentState {
    /**
     * The initial state for a new race.
     */
    object Idle: NewRaceIntentState()

    /**
     * A state for beginning a new countdown toward a new race.
     */
    data class NewCountdown(
        val countdownLocation: PlayerPosition
    ): NewRaceIntentState()

    /**
     * A state for an early disqualification, during the countdown process.
     */
    data class CancelRace(
        val reason: String,
        val resourceError: ResourceError? = null
    ): NewRaceIntentState() {
        companion object {
            const val CANCEL_RACE_REASON_NO_LOCATION = "no-location"
            const val CANCEL_RACE_SERVER_REFUSED = "server-refused"
            const val CANCEL_FALSE_START = "start-point-not-perfect"
            const val CANCELLED_BY_USER = "cancelled-by-user"
        }
    }
}