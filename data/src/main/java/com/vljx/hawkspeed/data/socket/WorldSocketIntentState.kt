package com.vljx.hawkspeed.data.socket

import com.vljx.hawkspeed.domain.models.world.PlayerPosition

sealed class WorldSocketIntentState {
    /**
     * A state for when a connection to the server can be opened/maintained.
     */
    data class CanJoinWorld(
        val deviceIdentifier: String,
        val entryToken: String,
        val gameServerInfo: String,
        val location: PlayerPosition
    ): WorldSocketIntentState()

    /**
     * A state for when a connection to the server should be closed/kept closed.
     */
    object CantJoinWorld: WorldSocketIntentState()
}