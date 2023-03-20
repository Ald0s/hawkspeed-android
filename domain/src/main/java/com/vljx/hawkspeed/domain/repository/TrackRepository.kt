package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.requests.track.GetTrackRequest
import com.vljx.hawkspeed.domain.requests.SubmitTrackRequest
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    /**
     * Return a flow that will constantly monitor all tracks stored in the local cache, and will emit a new view whenever a change is made.
     */
    fun getCachedTracks(): Flow<List<Track>>

    /**
     * Return a flow for a specific track, without its path.
     */
    fun getTrack(getTrackRequest: GetTrackRequest): Flow<Resource<Track>>

    /**
     * Submit the track in the SubmitTrackRequest. This will also cache the new track, without its path.
     */
    suspend fun submitNewTrack(submitTrackRequest: SubmitTrackRequest): Flow<Resource<Track>>

    /**
     * Cache the given tracks.
     */
    suspend fun cacheTracks(tracks: List<Track>)
}