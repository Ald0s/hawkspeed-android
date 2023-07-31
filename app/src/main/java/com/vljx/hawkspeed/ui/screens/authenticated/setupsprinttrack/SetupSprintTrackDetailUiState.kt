package com.vljx.hawkspeed.ui.screens.authenticated.setupsprinttrack

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.track.TrackWithPath

sealed class SetupSprintTrackDetailUiState {
    /**
     * The success state; the track was successfully submitted and created.
     */
    data class SprintTrackCreated(
        val trackWithPath: TrackWithPath
    ): SetupSprintTrackDetailUiState()

    /**
     * The state that indicates the setup track detail UI can be shown.
     */
    data class ShowSprintDetailForm(
        val trackDraftWithPoints: TrackDraftWithPoints,
        val setupSprintTrackDetailFormUiState: SetupSprintTrackDetailFormUiState
    ): SetupSprintTrackDetailUiState()

    /**
     * The initial loading state.
     */
    object Loading: SetupSprintTrackDetailUiState()

    /**
     * The failure state, used when there as an issue with the initial loading process.
     */
    data class LoadFailed(
        val resourceError: ResourceError
    ): SetupSprintTrackDetailUiState()
}