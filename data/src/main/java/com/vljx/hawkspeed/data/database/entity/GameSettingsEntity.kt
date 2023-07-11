package com.vljx.hawkspeed.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_settings")
data class GameSettingsEntity(
    @PrimaryKey
    val settingsId: Long?,
    val gameEntryToken: String?,
    val gameServerInfo: String?,
    val canConnectToGame: Boolean
)