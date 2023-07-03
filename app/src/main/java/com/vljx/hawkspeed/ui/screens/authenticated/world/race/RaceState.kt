package com.vljx.hawkspeed.ui.screens.authenticated.world.race

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.models.world.PlayerPosition

/**
 * A state that indicates the existence and basic status of a specific race instance. This is to be used to wrap the result of querying a race from cache.
 */
sealed class RaceState {
    /**
     * The state that communicates there is an ongoing race, right now.
     */
    data class Racing(
        val race: Race
    ): RaceState()

    /**
     * A state that communicates the progress of a start race attempt.
     */
    data class StartingRace(
        val trackUid: String,
        val vehicleUid: String,
        val currentSecond: Int,
        val countdownLocation: PlayerPosition
    ): RaceState()

    /**
     * The state that communicates a race that is no longer ongoing, but refers to the race just completed, cancelled or disqualified.
     */
    data class NotRacing(
        val race: Race
    ): RaceState()

    /**
     * A state that communicates a race start was attempted, but has recently failed. The container itself contains a few nullable
     * arguments for all the possible causes.
     */
    data class FailedStart(
        val reasonString: String,
        val resourceError: ResourceError?
    ): RaceState()

    /**
     * The default object, there is no ongoing race.
     */
    object NoRace: RaceState()
}