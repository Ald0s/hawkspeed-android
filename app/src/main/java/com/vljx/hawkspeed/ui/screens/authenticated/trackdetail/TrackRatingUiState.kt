package com.vljx.hawkspeed.ui.screens.authenticated.trackdetail

sealed class TrackRatingUiState {
    /**
     * The loading state for the rating UI. This should disable any clicking of either button.
     */
    object Loading: TrackRatingUiState()

    /**
     * The success state for the track rating's UI.
     */
    data class GotTrackRating(
        val trackUid: String,
        val numPositiveVotes: Int,
        val numNegativeVotes: Int,
        val yourRating: Boolean?
    ): TrackRatingUiState()
}