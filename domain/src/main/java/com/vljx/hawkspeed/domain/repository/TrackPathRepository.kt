package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import kotlinx.coroutines.flow.Flow

interface TrackPathRepository {
    /**
     *
     */
    fun getTrackPath(getTrackPathRequest: GetTrackPathRequest): Flow<Resource<TrackPath>>

    /**
     * Gets all tracks cached, with their paths where they are stored too.
     */
    fun getTracksWithPaths(): Flow<List<TrackWithPath>>
}