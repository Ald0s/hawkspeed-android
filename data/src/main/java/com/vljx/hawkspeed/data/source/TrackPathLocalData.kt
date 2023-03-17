package com.vljx.hawkspeed.data.source

import com.vljx.hawkspeed.data.models.track.TrackPathModel
import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import kotlinx.coroutines.flow.Flow

interface TrackPathLocalData {
    fun selectTrackPath(getTrackPathRequest: GetTrackPathRequest): Flow<TrackPathModel?>
    fun selectTracksWithPath(): Flow<List<TrackWithPathModel>>
    //fun selectTrackWithPath(getTrackPathRequest: GetTrackPathRequest): Flow<TrackWithPathModel?>

    suspend fun upsertTrackPath(trackPathModel: TrackPathModel)
}