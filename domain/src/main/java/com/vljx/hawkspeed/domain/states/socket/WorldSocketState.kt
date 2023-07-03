package com.vljx.hawkspeed.domain.states.socket

import com.vljx.hawkspeed.domain.ResourceError

sealed class WorldSocketState {
    /**
     * A state for when the socket has made a connection.
     */
    data class Connected(
        val playerUid: String,
        val startLatitude: Double,
        val startLongitude: Double,
        val startRotation: Float
    ): WorldSocketState()

    /**
     * A state for when the socket is attempting a connection.
     */
    object Connecting: WorldSocketState()

    /**
     * A state for when connection to the game server was actively terminated by the server. This different from the disconnected state because when this state
     * is used, the implication is that the problem isn't as simple to solve as just 'retrying'.
     */
    data class ConnectionRefused(
        val resourceError: ResourceError? = null
    ): WorldSocketState()

    /**
     * A state for when the socket has been disconnected, optionally given an error. A null error object may indicate that the disconnection was prompted by the
     * client itself. This disconnection state will allow reconnection.
     */
    data class Disconnected(
        val resourceError: ResourceError? = null
    ): WorldSocketState()
}