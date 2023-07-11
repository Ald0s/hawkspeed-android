package com.vljx.hawkspeed.ui.screens.authenticated.world.race

import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation

/**
 * A state for describing a Player's position relative to the start line of a track.
 */
sealed class StartLineState {
    /**
     * A state that indicates the Player is in the perfect position and orientation to race the track.
     */
    data class Perfect(
        val location: PlayerPositionWithOrientation
    ): StartLineState()

    /**
     * A state that indicates the Player is not in the perfect condition or orientation, but is still able to move such that they are in the perfect position.
     * This state should disable controls that allow the race to start.
     */
    data class Standby(
        val location: PlayerPositionWithOrientation
    ): StartLineState()

    /**
     * A state that indicates the Player has rejected the potential race for the track, and the world map race screen should be exited.
     */
    data class MovedAway(
        val location: PlayerPositionWithOrientation
    ): StartLineState()

    /**
     * A state that indicates dependant resources could not be loaded just yet so start line state is inconclusive.
     */
    object Inconclusive: StartLineState()
}