package com.vljx.hawkspeed.ui.screens.authenticated.setuptrack

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.ui.component.InputValidationResult

sealed class SetupTrackDetailFormUiState {
    /**
     * A state that contains the current state of all arguments required for the track detail.
     */
    data class TrackDetailForm(
        val validateTrackName: InputValidationResult,
        val validateTrackDescription: InputValidationResult,
        val canAttemptSubmitTrack: Boolean,
    ): SetupTrackDetailFormUiState()

    /**
     * A state that indicates a new track detail is being submitted.
     */
    object Submitting: SetupTrackDetailFormUiState()

    /**
     * A state that indicates an error with submitting the new track, reported by the server.
     */
    data class ServerRefused(
        val resourceError: ResourceError
    ): SetupTrackDetailFormUiState()
}