package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.TrackPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TrackPointDao: BaseDao<TrackPointEntity>() {
    @Query("""
        SELECT *
        FROM track_point
        WHERE track_point.trackUid = :trackUid
        ORDER BY track_point.trackPointId ASC
    """)
    abstract fun selectPointsForTrackUid(trackUid: String): Flow<List<TrackPointEntity>?>

    @Query("""
        DELETE FROM track_point
        WHERE trackUid = :trackUid
    """)
    abstract suspend fun clearAllPointsFor(trackUid: String)
}