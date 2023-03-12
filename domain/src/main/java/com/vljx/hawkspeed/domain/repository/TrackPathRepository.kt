package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import kotlinx.coroutines.flow.Flow

interface TrackPathRepository {
    /**
     *
     */
    fun getTrackPath(getTrackPathRequest: GetTrackPathRequest): Flow<Resource<TrackPath>>
}