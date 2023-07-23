package com.vljx.hawkspeed.ui.screens.dialogs.trackpreview

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.ui.screens.authenticated.trackdetail.TrackRatingUiState

sealed class TrackPreviewUiState {
    /**
     * A success state; the latest outcome of the track preview, exactly how it should be displayed.
     */
    data class TrackPreview(
        val track: Track,
        val raceModePromptUiState: RaceModePromptUiState
    ): TrackPreviewUiState()

    /**
     * At the very least, the track is currently loading.
     */
    object Loading: TrackPreviewUiState()

    /**
     * An error state that indicates the loading of the track preview failed.
     */
    data class Failed(
        val resourceError: ResourceError
    ): TrackPreviewUiState()
}