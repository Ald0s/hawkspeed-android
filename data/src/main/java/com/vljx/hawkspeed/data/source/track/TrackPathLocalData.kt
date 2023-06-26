package com.vljx.hawkspeed.data.source.track

import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import com.vljx.hawkspeed.domain.requestmodels.track.RequestDeleteTrackAndPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import kotlinx.coroutines.flow.Flow

interface TrackPathLocalData {
    /**
     * Select a Track with its path, if the path is cached. This will open a flow to that track & its path.
     */
    fun selectTrackWithPath(requestGetTrackWithPath: RequestGetTrackWithPath): Flow<TrackWithPathModel?>

    /**
     * Select all tracks with their paths, where paths are cached. This will open a flow to all tracks and their paths.
     */
    fun selectTracksWithPaths(): Flow<List<TrackWithPathModel>>

    /**
     * Upsert a track with its path.
     */
    suspend fun upsertTrackWithPath(trackWithPath: TrackWithPathModel)

    /**
     * Delete the requested track, along with its track path and points, if this is present.
     */
    suspend fun deleteTrackAndPath(requestDeleteTrackAndPath: RequestDeleteTrackAndPath)
}