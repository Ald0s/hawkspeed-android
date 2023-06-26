package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.track.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TrackDao: BaseDao<TrackEntity>() {
    @Query("""
        SELECT *
        FROM track
    """)
    abstract fun selectAllTracks(): Flow<List<TrackEntity>>

    @Query("""
        SELECT *
        FROM track
        WHERE track.trackUid = :trackUid
    """)
    abstract fun selectTrackByUid(trackUid: String): Flow<TrackEntity?>

    @Query("""
        DELETE FROM track
        WHERE trackUid = :trackUid
    """)
    abstract suspend fun deleteByUid(trackUid: String)
}