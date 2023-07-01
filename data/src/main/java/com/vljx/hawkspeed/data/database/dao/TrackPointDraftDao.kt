package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.track.TrackPointDraftEntity

@Dao
abstract class TrackPointDraftDao: BaseDao<TrackPointDraftEntity>() {
    @Query("""
        DELETE FROM track_point_draft
        WHERE trackDraftId = :trackDraftId
    """)
    abstract suspend fun clearPointsFor(trackDraftId: Long)
}