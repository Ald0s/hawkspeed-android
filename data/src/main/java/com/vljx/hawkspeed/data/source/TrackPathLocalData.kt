package com.vljx.hawkspeed.data.source

import com.vljx.hawkspeed.data.models.track.TrackPathModel
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import kotlinx.coroutines.flow.Flow

interface TrackPathLocalData {
    fun selectTrackPath(getTrackPathRequest: GetTrackPathRequest): Flow<TrackPathModel?>

    suspend fun upsertTrackPath(trackPathModel: TrackPathModel)
}