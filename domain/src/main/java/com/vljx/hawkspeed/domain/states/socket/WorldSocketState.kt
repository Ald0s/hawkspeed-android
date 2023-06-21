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
     * A state for when the socket has been disconnected, optionally given an error. A null error object may indicate that the
     * disconnection was prompted by the client itself.
     */
    data class Disconnected(
        val resourceError: ResourceError? = null
    ): WorldSocketState()

}