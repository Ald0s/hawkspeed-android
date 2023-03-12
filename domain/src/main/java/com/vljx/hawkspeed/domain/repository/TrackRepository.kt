package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.requests.track.GetTrackRequest
import com.vljx.hawkspeed.domain.requests.SubmitTrackRequest
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    /**
     *
     */
    fun getTrack(getTrackRequest: GetTrackRequest): Flow<Resource<Track>>

    /**
     *
     */
    suspend fun submitNewTrack(submitTrackRequest: SubmitTrackRequest): Flow<Resource<Track>>
}