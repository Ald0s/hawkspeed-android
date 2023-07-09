package com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack

import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints

sealed class WorldMapRecordTrackUiState {
    /**
     * The recording state. Use this when the track is being recorded. Options to stop the ongoing track recording, and to cancel the track recording should be present.
     * This state will be updated with the latest track with points, after the latest point is inserted.
     */
    data class Recording(
        val trackDraftWithPoints: TrackDraftWithPoints,
        val totalLength: String
    ): WorldMapRecordTrackUiState()

    /**
     * The new track state. Use this when a new track has been created. Options to start recording the new track, and to cancel the track recording should be present.
     */
    data class NewTrack(
        val trackDraftWithPoints: TrackDraftWithPoints
    ): WorldMapRecordTrackUiState()

    /**
     * The overview state. Use this when a track is existing, but the Player is not recording. This state should set the view over the entire track that has already
     * been recorded, sort of like an overview of progress. Also, this state should allow clearing the current track, finalising details on that track, or stopping
     * the recording of the track.
     */
    data class RecordedTrackOverview(
        val trackDraftWithPoints: TrackDraftWithPoints,
        val totalLength: String
    ): WorldMapRecordTrackUiState()

    /**
     * The 'complete' state. This is to be used when the User indicates they are happy with the track and wish to proceed to setting the track up.
     */
    data class RecordingComplete(
        val trackDraftWithPoints: TrackDraftWithPoints
    ): WorldMapRecordTrackUiState()

    /**
     * The initial loading state.
     */
    object Loading: WorldMapRecordTrackUiState()

    /**
     * A state that indicates the track draft has been saved or deleted. This should trigger an exit from the recording screen.
     */
    data class RecordingCancelled(
        val trackDraftWithPoints: TrackDraftWithPoints,
        val savedToCache: Boolean
    ): WorldMapRecordTrackUiState()
}