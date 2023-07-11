package com.vljx.hawkspeed.ui.screens.authenticated.world.race

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation

sealed class NewRaceIntentState {
    /**
     * The initial state for a new race.
     */
    object Idle: NewRaceIntentState()

    /**
     * A state for beginning a new countdown toward a new race.
     */
    data class NewCountdown(
        val trackUid: String,
        val vehicleUid: String,
        val countdownLocationWithOrientation: PlayerPositionWithOrientation
    ): NewRaceIntentState()

    /**
     * A state for an early disqualification, during the countdown process.
     */
    data class CancelRaceStart(
        val reason: String,
        val resourceError: ResourceError? = null
    ): NewRaceIntentState()
}