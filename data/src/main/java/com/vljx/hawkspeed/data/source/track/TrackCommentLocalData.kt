package com.vljx.hawkspeed.data.source.track

import androidx.paging.PagingSource
import com.vljx.hawkspeed.data.database.entity.track.TrackCommentEntity
import com.vljx.hawkspeed.data.models.track.TrackCommentsPageModel
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackComments

interface TrackCommentLocalData {
    /**
     * Return a paging source for all comments currently cached, that are associated with the given Track.
     */
    fun pageCommentsForTrack(requestPageTrackComments: RequestPageTrackComments): PagingSource<Int, TrackCommentEntity>

    /**
     * Upsert the page of comments received.
     */
    suspend fun upsertTrackCommentsPage(trackCommentsPage: TrackCommentsPageModel)

    /**
     * Clear all comments from cache that are associated with the given track.
     */
    suspend fun clearCommentsFor(trackUid: String)
}