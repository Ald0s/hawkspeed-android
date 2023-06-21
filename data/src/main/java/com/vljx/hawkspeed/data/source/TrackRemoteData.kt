package com.vljx.hawkspeed.data.source

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrack

interface TrackRemoteData {
    /**
     * Query a track given the request.
     */
    suspend fun queryTrack(requestGetTrack: RequestGetTrack): Resource<TrackModel>

    /**
     * Perform a query to request the creation of a new track.
     */
    suspend fun createNewTrack(requestSubmitTrack: RequestSubmitTrack): Resource<TrackModel>
}