package com.vljx.hawkspeed.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.track.TrackCommentEntity

@Dao
abstract class TrackCommentDao: BaseDao<TrackCommentEntity>() {
    @Query("""
        SELECT *
        FROM track_comment
        
        INNER JOIN track
        ON track.trackUid = track_comment.trackUid
        
        WHERE track.trackUid = :trackUid
        ORDER BY track_comment.created DESC
    """)
    abstract fun pageCommentsFromTrack(trackUid: String): PagingSource<Int, TrackCommentEntity>

    @Query("""
        DELETE FROM track_comment
        WHERE track_comment.trackUid = :trackUid
    """)
    abstract suspend fun clearCommentsFor(trackUid: String)
}