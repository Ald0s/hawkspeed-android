package com.vljx.hawkspeed.data.source

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requests.SubmitTrackRequest
import com.vljx.hawkspeed.domain.requests.track.GetTrackRequest

interface TrackRemoteData {
    suspend fun queryTrack(getTrackRequest: GetTrackRequest): Resource<TrackModel>

    suspend fun createNewTrack(submitTrackRequest: SubmitTrackRequest): Resource<TrackModel>
}