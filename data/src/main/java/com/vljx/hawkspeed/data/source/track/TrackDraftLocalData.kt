package com.vljx.hawkspeed.data.source.track

import com.vljx.hawkspeed.data.models.track.TrackDraftWithPointsModel
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestAddTrackPointDraft
import kotlinx.coroutines.flow.Flow

interface TrackDraftLocalData {
    /**
     * Open a flow to the cache for an existing track draft, along with all its points, by its id.
     */
    fun selectTrackDraftById(trackDraftId: Long): Flow<TrackDraftWithPointsModel?>

    /**
     * Create a new track draft with points, then return a flow for it.
     */
    fun newTrackDraftWithPoints(): Flow<TrackDraftWithPointsModel>

    /**
     * Insert the given location as the latest point in the desired track draft, then return the up to date track draft with points. If the track draft
     * identified by the Id does not exist, this function will fail with an exception.
     */
    suspend fun addPointToTrack(requestAddTrackPointDraft: RequestAddTrackPointDraft): TrackDraftWithPointsModel

    /**
     * Insert the given track draft and all associated points into cache.
     */
    suspend fun insertTrackDraftWithPoints(trackDraftWithPoints: TrackDraftWithPointsModel): Long

    /**
     * Upsert the given track draft into cache, along with all points.
     */
    suspend fun upsertTrackDraftWithPoints(trackDraftWithPoints: TrackDraftWithPointsModel)

    /**
     * Delete all point drafts attached to the given track draft, then return the track draft with points.
     */
    suspend fun clearPointsForDraft(trackDraftId: Long): TrackDraftWithPointsModel

    /**
     * Delete a track draft, along with all associated points, by its id.
     */
    suspend fun deleteTrackDraftWithPointsById(trackDraftId: Long)
}