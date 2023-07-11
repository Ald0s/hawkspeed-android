package com.vljx.hawkspeed.data.models.account

data class GameSettingsModel(
    val gameEntryToken: String?,
    val gameServerInfo: String?,
    val canConnectToGame: Boolean
)