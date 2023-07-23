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
     * A state for when the socket is attempting a connection or for when the socket is attempting a reconnection. Supply the resource error only in the latter case.
     */
    data class Connecting(
        val resourceError: ResourceError? = null
    ): WorldSocketState()

    /**
     * A state for when the socket is in the disconnected state. This can be invoked for one of two reasons; when 'resourceError' is null, this is the natural default disconnected
     * state and essentially invites a connection attempt. If resourceError is not null, this disconnection has been actively invoked and therefore must be cleared by setting the
     * state back to a blank disconnected state to proceed.
     */
    data class Disconnected(
        val resourceError: ResourceError? = null
    ): WorldSocketState()
}