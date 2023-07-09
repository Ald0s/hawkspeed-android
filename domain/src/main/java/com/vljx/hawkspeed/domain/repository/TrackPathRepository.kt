package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestDeleteTrackAndPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack
import kotlinx.coroutines.flow.Flow

interface TrackPathRepository {
    /**
     * Get a track with its path. This function will open a flow, perform a query for the track and its path, then cache both.
     */
    fun getTrackWithPath(requestGetTrackWithPath: RequestGetTrackWithPath): Flow<Resource<TrackWithPath>>

    /**
     * Submit the track in the SubmitTrackRequest. The returned value will be a TrackWithPath, containing a Track (for sure) and possibly a TrackPath.
     * The distinction here is based on whether or not the track path can be verified immediately; like if server isn't snapping to roads or a feature
     * such as admin approval is disabled. Either way, path may or may not actually be returned.
     */
    fun submitNewTrack(requestSubmitTrack: RequestSubmitTrack): Flow<Resource<TrackWithPath>>

    /**
     * Get all tracks and their optional paths. This function will open a flow, only to cache. No network queries will be performed.
     */
    fun getTracksWithPathsFromCache(): Flow<List<TrackWithPath>>

    /**
     * Delete the given track, with its path and points, if applicable.
     */
    suspend fun deleteTrackAndPath(requestDeleteTrackAndPath: RequestDeleteTrackAndPath)
}