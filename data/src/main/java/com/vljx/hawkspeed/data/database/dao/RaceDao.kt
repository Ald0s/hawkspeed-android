package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.RaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RaceDao: BaseDao<RaceEntity>() {
    @Query("""
        SELECT *
        FROM race
        WHERE raceUid = :raceUid
    """)
    abstract fun selectRace(raceUid: String): Flow<RaceEntity?>

    @Query("""
        DELETE FROM race
        WHERE raceUid = :raceUid
    """)
    abstract suspend fun deleteRace(raceUid: String)
}