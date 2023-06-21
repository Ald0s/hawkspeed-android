package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrack
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    /**
     * Open a flow to the cache and also to the network for the most up to date entry of a Track without its path.
     */
    fun getTrack(getTrack: RequestGetTrack): Flow<Resource<Track>>

    /**
     * Submit the track in the SubmitTrackRequest. This will also cache the new track, without its path.
     */
    fun submitNewTrack(requestSubmitTrack: RequestSubmitTrack): Flow<Resource<Track>>
}