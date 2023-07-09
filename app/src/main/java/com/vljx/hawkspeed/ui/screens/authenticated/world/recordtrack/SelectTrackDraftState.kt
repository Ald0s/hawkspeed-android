package com.vljx.hawkspeed.ui.screens.authenticated.world.recordtrack

import com.vljx.hawkspeed.domain.enums.TrackType

sealed class SelectTrackDraftState {
    /**
     * A state the indicates a track should be created with the specified track type. This should, if used, result in a new track being created,
     * and it being mapped to a selected track draft.
     */
    data class CreateTrackDraft(
        val chosenTrackType: TrackType
    ) : SelectTrackDraftState()

    /**
     * A state that indicates an existing track draft should be used. This will result in the track draft being queried from cache.
     */
    data class SelectTrackDraft(
        val trackDraftId: Long
    ) : SelectTrackDraftState()
}