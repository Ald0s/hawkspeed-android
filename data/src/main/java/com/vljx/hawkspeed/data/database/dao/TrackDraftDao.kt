package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.vljx.hawkspeed.data.database.entity.track.TrackDraftEntity
import com.vljx.hawkspeed.data.database.entity.track.TrackDraftWithPointsEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TrackDraftDao: BaseDao<TrackDraftEntity>() {
    @Transaction
    @Query("""
        SELECT *
        FROM track_draft
        WHERE trackDraftId = :trackDraftId
    """)
    abstract fun selectTrackDraftWithPoints(trackDraftId: Long): Flow<TrackDraftWithPointsEntity?>

    @Query("""
        DELETE FROM track_draft
        WHERE trackDraftId = :trackDraftId
    """)
    abstract suspend fun deleteTrackDraft(trackDraftId: Long)
}