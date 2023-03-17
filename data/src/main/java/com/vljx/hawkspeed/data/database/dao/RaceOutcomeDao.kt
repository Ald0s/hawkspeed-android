package com.vljx.hawkspeed.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.RaceOutcomeEntity

@Dao
abstract class RaceOutcomeDao: BaseDao<RaceOutcomeEntity>() {
    @Query("""
        SELECT *
        FROM race_outcome
        
        INNER JOIN track
        ON track.trackUid = race_outcome.trackUid
        
        WHERE track.trackUid = :trackUid
        ORDER BY race_outcome.stopwatch ASC
    """)
    abstract fun pageRaceOutcomesFromTrack(trackUid: String): PagingSource<Int, RaceOutcomeEntity>

    @Query("""
        DELETE FROM race_outcome
        WHERE race_outcome.trackUid = :trackUid
    """)
    abstract suspend fun clearRaceOutcomesFor(trackUid: String)
}