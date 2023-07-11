package com.vljx.hawkspeed.data.source.account

import com.vljx.hawkspeed.data.models.account.GameSettingsModel
import kotlinx.coroutines.flow.Flow

interface GameSettingsLocalData {
    /**
     * Open a flow for the current game settings instance.
     */
    fun selectGameSettings(): Flow<GameSettingsModel?>

    /**
     * Upsert a game settings model.
     */
    suspend fun upsertGameSettings(gameSettingsModel: GameSettingsModel)

    /**
     * Delete the game settings model.
     */
    suspend fun clearGameSettings()
}