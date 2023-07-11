package com.vljx.hawkspeed.ui.screens.dialogs.trackpreview

import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation

sealed class RaceModePromptUiState {
    /**
     * The state for when this track actually can be raced from where the User currently is. All information necessary for a start at this time
     * is provided by the state data class itself.
     */
    data class CanEnterRaceMode(
        val trackUid: String,
        val locationWithOrientation: PlayerPositionWithOrientation
    ): RaceModePromptUiState()

    /**
     * The state for when the track can't be raced. This is the default.
     */
    object CantEnterRaceMode: RaceModePromptUiState()
}