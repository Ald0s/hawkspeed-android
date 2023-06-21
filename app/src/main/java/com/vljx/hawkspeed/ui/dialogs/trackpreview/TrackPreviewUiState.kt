package com.vljx.hawkspeed.ui.dialogs.trackpreview

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.track.Track

sealed class TrackPreviewUiState {
    // The loading state, which is the default state since the composable will launch without a suitable UID.
    object Loading: TrackPreviewUiState()

    // The success state, the latest track has arrived.
    data class GotTrack(
        val track: Track
    ): TrackPreviewUiState()

    // The error state.
    data class Failed(
        val resourceError: ResourceError
    ): TrackPreviewUiState()
}