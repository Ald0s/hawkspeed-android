package com.vljx.hawkspeed.data.source.track

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack

interface TrackPathRemoteData {
    /**
     * Perform a query for a track with its path.
     */
    suspend fun queryTrackWithPath(requestGetTrackWithPath: RequestGetTrackWithPath): Resource<TrackWithPathModel>

    /**
     * Perform a query to request the creation of a new track.
     */
    suspend fun createNewTrack(requestSubmitTrack: RequestSubmitTrack): Resource<TrackWithPathModel>
}