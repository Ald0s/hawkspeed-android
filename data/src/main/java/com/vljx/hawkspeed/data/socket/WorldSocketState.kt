package com.vljx.hawkspeed.data.socket

sealed class WorldSocketState {
    object Disconnected: WorldSocketState()
    object Connecting: WorldSocketState()
    data class Connected(
        val playerUid: String,
        val latitude: Double,
        val longitude: Double,
        val rotation: Float
    ): WorldSocketState()
}