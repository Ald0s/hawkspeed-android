package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.vljx.hawkspeed.data.database.entity.track.TrackPathEntity
import com.vljx.hawkspeed.data.database.entity.track.TrackWithPathEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TrackPathDao: BaseDao<TrackPathEntity>() {
    @Transaction
    @Query("""
        SELECT *
        FROM track 
        WHERE trackUid = :trackUid
    """)
    abstract fun selectTrackWithPath(trackUid: String): Flow<TrackWithPathEntity?>

    @Transaction
    @Query("""
        SELECT *
        FROM track
    """)
    abstract fun selectTracksWithPaths(): Flow<List<TrackWithPathEntity>>

    @Query("""
        DELETE FROM track_path
        WHERE trackPathUid = :trackPathUid
    """)
    abstract suspend fun deletePathByUid(trackPathUid: String)
}