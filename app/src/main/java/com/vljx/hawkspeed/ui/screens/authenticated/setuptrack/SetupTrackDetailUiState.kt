package com.vljx.hawkspeed.ui.screens.authenticated.setuptrack

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.track.TrackWithPath

sealed class SetupTrackDetailUiState {
    /**
     * The initial state; idle.
     */
    object Idle: SetupTrackDetailUiState()

    /**
     * The submitting/loading state.
     */
    object Loading: SetupTrackDetailUiState()

    /**
     * The success state; the track was successfully submitted and created.
     */
    data class TrackCreated(
        val trackWithPath: TrackWithPath
    ): SetupTrackDetailUiState()

    /**
     * The failure state; the track couldn't be created.
     */
    data class Failed(
        val resourceError: ResourceError
    ): SetupTrackDetailUiState()
}