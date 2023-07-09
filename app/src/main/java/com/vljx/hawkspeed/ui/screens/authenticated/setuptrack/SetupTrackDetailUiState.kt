package com.vljx.hawkspeed.ui.screens.authenticated.setuptrack

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.ui.component.InputValidationResult

sealed class SetupTrackDetailUiState {
    /**
     * The success state; the track was successfully submitted and created.
     */
    data class TrackCreated(
        val trackWithPath: TrackWithPath
    ): SetupTrackDetailUiState()

    /**
     * The state that indicates the setup track detail UI can be shown.
     */
    data class ShowDetailForm(
        val trackDraftWithPoints: TrackDraftWithPoints,
        val setupTrackDetailFormUiState: SetupTrackDetailFormUiState
    ): SetupTrackDetailUiState()

    /**
     * The initial loading state.
     */
    object Loading: SetupTrackDetailUiState()

    /**
     * The failure state, used when there as an issue with the initial loading process.
     */
    data class LoadFailed(
        val resourceError: ResourceError
    ): SetupTrackDetailUiState()
}