package com.vljx.hawkspeed.ui.dialogs.trackpreview

import com.vljx.hawkspeed.domain.models.world.PlayerPosition

sealed class RacePromptUiState {
    /**
     * The state for when the track can't be raced. This is the default.
     */
    object CantRace: RacePromptUiState()

    /**
     * The state for when this track actually can be raced from where the User currently is. All information necessary for a start at this time
     * is provided by the state data class itself.
     */
    data class CanRace(
        val trackUid: String,
        val location: PlayerPosition
    ): RacePromptUiState()
}