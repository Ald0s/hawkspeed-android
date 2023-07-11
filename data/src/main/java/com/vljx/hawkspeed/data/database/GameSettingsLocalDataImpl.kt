package com.vljx.hawkspeed.data.database

import com.vljx.hawkspeed.data.database.dao.GameSettingsDao
import com.vljx.hawkspeed.data.database.mapper.GameSettingsEntityMapper
import com.vljx.hawkspeed.data.models.account.GameSettingsModel
import com.vljx.hawkspeed.data.source.account.GameSettingsLocalData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GameSettingsLocalDataImpl @Inject constructor(
    private val gameSettingsDao: GameSettingsDao,

    private val gameSettingsEntityMapper: GameSettingsEntityMapper
): GameSettingsLocalData {
    override fun selectGameSettings(): Flow<GameSettingsModel?> =
        gameSettingsDao.selectGameSettings().map { gameSettingsEntity ->
            gameSettingsEntity?.let { gameSettingsEntityMapper.mapFromEntity(it) }
        }

    override suspend fun upsertGameSettings(gameSettingsModel: GameSettingsModel) =
        gameSettingsDao.upsert(
            gameSettingsEntityMapper.mapToEntity(gameSettingsModel)
        )

    override suspend fun clearGameSettings() =
        gameSettingsDao.clearGameSettings()
}