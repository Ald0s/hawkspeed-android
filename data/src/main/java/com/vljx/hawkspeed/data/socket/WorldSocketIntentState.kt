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
        val location: PlayerPosition,
        val reconnectionAttempts: Int = 10,
        val reconnectionDelay: Long = 10000
    ): WorldSocketIntentState()

    /**
     * A state for when a connection to the server should be closed/kept closed.
     */
    object CantJoinWorld: WorldSocketIntentState()
}