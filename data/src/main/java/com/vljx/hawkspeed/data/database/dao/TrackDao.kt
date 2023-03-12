package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TrackDao: BaseDao<TrackEntity>() {
    @Query("""
        SELECT *
        FROM track
        WHERE track.trackUid = :trackUid
    """)
    abstract fun selectTrackByUid(trackUid: String): Flow<TrackEntity?>
}