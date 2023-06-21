package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestDeleteTrackAndPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import kotlinx.coroutines.flow.Flow

interface TrackPathRepository {
    /**
     * Get a track with its path. This function will open a flow, perform a query for the track and its path, then cache both.
     */
    fun getTrackWithPath(requestGetTrackWithPath: RequestGetTrackWithPath): Flow<Resource<TrackWithPath>>

    /**
     * Get all tracks and their optional paths. This function will open a flow, only to cache. No network queries will be performed.
     */
    fun getTracksWithPathsFromCache(): Flow<List<TrackWithPath>>

    /**
     * Delete the given track, with its path and points, if applicable.
     */
    suspend fun deleteTrackAndPath(requestDeleteTrackAndPath: RequestDeleteTrackAndPath)
}