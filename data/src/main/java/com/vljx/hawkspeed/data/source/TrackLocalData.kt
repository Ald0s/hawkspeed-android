package com.vljx.hawkspeed.data.source

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.models.track.TrackPathModel
import com.vljx.hawkspeed.domain.requests.track.GetTrackRequest
import kotlinx.coroutines.flow.Flow

interface TrackLocalData {
    fun selectTracks(): Flow<List<TrackModel>>
    fun selectTrack(getTrackRequest: GetTrackRequest): Flow<TrackModel?>

    suspend fun upsertTrack(trackModel: TrackModel)
    suspend fun upsertTracks(trackModels: List<TrackModel>)
}