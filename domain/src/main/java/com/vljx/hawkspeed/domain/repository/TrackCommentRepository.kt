package com.vljx.hawkspeed.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.comment.Comment
import com.vljx.hawkspeed.domain.models.trackcomment.TrackComments
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackComments
import com.vljx.hawkspeed.domain.requestmodels.track.RequestTrackLatestComments
import kotlinx.coroutines.flow.Flow

interface TrackCommentRepository {
    /**
     * Paginate the comments on the given Track.
     */
    @ExperimentalPagingApi
    fun pageCommentsForTrack(requestPageTrackComments: RequestPageTrackComments): Flow<PagingData<Comment>>

    /**
     * Request the very first page of comments on this track, that is, the latest.
     */
    fun getLatestCommentsForTrack(requestTrackLatestComments: RequestTrackLatestComments): Flow<Resource<TrackComments>>
}