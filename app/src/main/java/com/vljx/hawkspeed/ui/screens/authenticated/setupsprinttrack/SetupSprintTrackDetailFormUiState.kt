package com.vljx.hawkspeed.ui.screens.authenticated.setupsprinttrack

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.ui.component.InputValidationResult

sealed class SetupSprintTrackDetailFormUiState {
    /**
     * A state that contains the current state of all arguments required for the track detail.
     */
    data class SprintTrackDetailForm(
        val validateTrackName: InputValidationResult,
        val validateTrackDescription: InputValidationResult,
        val canAttemptSubmitTrack: Boolean,
    ): SetupSprintTrackDetailFormUiState()

    /**
     * A state that indicates a new track detail is being submitted.
     */
    object Submitting: SetupSprintTrackDetailFormUiState()

    /**
     * A state that indicates an error with submitting the new track, reported by the server.
     */
    data class ServerRefused(
        val resourceError: ResourceError
    ): SetupSprintTrackDetailFormUiState()
}