package com.vljx.hawkspeed.data.source.track

import com.vljx.hawkspeed.data.models.track.TrackModel
import kotlinx.coroutines.flow.Flow

interface TrackLocalData {
    /**
     * Select a Track, opening a flow, without its path, by its UID.
     */
    fun selectTrackByUid(trackUid: String): Flow<TrackModel?>

    /**
     * Upsert the given track's model into cache.
     */
    suspend fun upsertTrack(track: TrackModel)

    /**
     * Upsert all given track models into cache.
     */
    suspend fun upsertTracks(tracks: List<TrackModel>)
}