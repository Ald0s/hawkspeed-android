package com.vljx.hawkspeed.data.source.track

import com.vljx.hawkspeed.data.models.track.TrackCommentsPageModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackComments

interface TrackCommentRemoteData {
    /**
     * Query the requested page of comments for the given Track.
     */
    suspend fun queryCommentPage(requestPageTrackComments: RequestPageTrackComments, page: Int): Resource<TrackCommentsPageModel>
}