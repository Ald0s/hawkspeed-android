package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestAddTrackPointDraft
import kotlinx.coroutines.flow.Flow

interface TrackDraftRepository {
    /**
     * Open a flow to cache targeting the track draft, along with all associated points, identified by the given Id.
     */
    fun getTrackDraftWithPointsById(trackDraftId: Long): Flow<TrackDraftWithPoints?>

    /**
     * Create a new track draft with points.
     */
    fun newTrackDraftWithPoints(): Flow<TrackDraftWithPoints?>

    /**
     * Save the given track draft with points to cache.
     */
    suspend fun saveTrackDraft(trackDraftWithPoints: TrackDraftWithPoints)

    /**
     * Add the given track point to the desired track draft, then return the latest track draft with points.
     */
    suspend fun addPointToTrackDraft(requestAddTrackPointDraft: RequestAddTrackPointDraft): TrackDraftWithPoints

    /**
     * Clear all point drafts associated with the desired track draft.
     */
    suspend fun clearPointsForTrackDraft(trackDraftId: Long): TrackDraftWithPoints

    /**
     * Delete the track draft, and all its points, identified by the Id.
     */
    suspend fun deleteTrackDraft(trackDraftId: Long)
}