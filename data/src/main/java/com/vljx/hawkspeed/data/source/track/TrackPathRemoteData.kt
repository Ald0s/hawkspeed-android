package com.vljx.hawkspeed.data.source.track

import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath

interface TrackPathRemoteData {
    /**
     * Perform a query for a track with its path.
     */
    suspend fun queryTrackWithPath(requestGetTrackWithPath: RequestGetTrackWithPath): Resource<TrackWithPathModel>
}