package com.vljx.hawkspeed.data.source

import com.vljx.hawkspeed.data.models.track.TrackPathModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest

interface TrackPathRemoteData {
    /**
     *
     */
    suspend fun queryTrackPath(getTrackPathRequest: GetTrackPathRequest): Resource<TrackPathModel>
}