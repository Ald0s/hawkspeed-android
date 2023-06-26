package com.vljx.hawkspeed.ui.screens.authenticated.trackdetail

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.track.Track

sealed class TrackDetailUiState {
    /**
     * The initial state for track detail while it loads.
     */
    object Loading: TrackDetailUiState()

    /**
     * A state that represents a successful load of the track's detail.
     */
    data class GotTrackDetail(
        val track: Track
    ): TrackDetailUiState()

    /**
     * A state that represents a failed load of the track's detail.
     */
    data class Failed(
        val resourceError: ResourceError
    ): TrackDetailUiState()
}