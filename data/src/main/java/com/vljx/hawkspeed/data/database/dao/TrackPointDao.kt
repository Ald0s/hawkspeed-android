package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.vljx.hawkspeed.data.database.entity.TrackPointEntity
import com.vljx.hawkspeed.data.database.relationships.TrackWithPoints
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TrackPointDao: BaseDao<TrackPointEntity>() {
    /*@Transaction
    @Query("""
        SELECT *
        FROM track
        WHERE trackUid = :trackUid
    """)
    abstract fun selectTrackWithPoints(trackUid: String): Flow<TrackWithPoints?>*/

    @Transaction
    @Query("""
        SELECT *
        FROM track
    """)
    abstract fun selectTracksWithPoints(): Flow<List<TrackWithPoints>>

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