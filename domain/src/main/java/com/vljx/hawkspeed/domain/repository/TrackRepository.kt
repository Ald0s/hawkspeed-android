package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestClearTrackRating
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrack
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSetTrackRating
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    /**
     * Open a flow to the cache and also to the network for the most up to date entry of a Track without its path.
     */
    fun getTrack(getTrack: RequestGetTrack): Flow<Resource<Track>>

    /**
     * Send a request to set the current User's rating on a track. This will automatically cache the result.
     */
    suspend fun setTrackRating(requestSetTrackRating: RequestSetTrackRating): Resource<Track>

    /**
     * Send a request to clear the current User's rating on a track. This will automatically cache the result.
     */
    suspend fun clearTrackRating(requestClearTrackRating: RequestClearTrackRating): Resource<Track>
}