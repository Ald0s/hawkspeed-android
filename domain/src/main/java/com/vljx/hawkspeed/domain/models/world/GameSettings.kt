package com.vljx.hawkspeed.domain.models.world

data class GameSettings(
    val canConnectGame: Boolean,
    val entryToken: String?,
    val gameServerInfo: String?
) {
    val isServerAvailable: Boolean
        get() = entryToken != null && gameServerInfo != null
}