package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.GameSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class GameSettingsDao: BaseDao<GameSettingsEntity>() {
    @Query("""
        SELECT *
        FROM game_settings
        LIMIT 1
    """)
    abstract fun selectGameSettings(): Flow<GameSettingsEntity?>

    @Query("""
        DELETE FROM game_settings
    """)
    abstract suspend fun clearGameSettings()
}