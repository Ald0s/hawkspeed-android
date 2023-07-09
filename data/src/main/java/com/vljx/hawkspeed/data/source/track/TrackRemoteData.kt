package com.vljx.hawkspeed.data.source.track

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestClearTrackRating
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrack
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSetTrackRating

interface TrackRemoteData {
    /**
     * Query a track given the request.
     */
    suspend fun queryTrack(requestGetTrack: RequestGetTrack): Resource<TrackModel>

    /**
     * Perform a query to set the current User's rating for the desired track.
     */
    suspend fun setTrackRating(requestSetTrackRating: RequestSetTrackRating): Resource<TrackModel>

    /**
     * Perform a query to clear the current User's rating for the desired track.
     */
    suspend fun clearTrackRating(requestClearTrackRating: RequestClearTrackRating): Resource<TrackModel>
}