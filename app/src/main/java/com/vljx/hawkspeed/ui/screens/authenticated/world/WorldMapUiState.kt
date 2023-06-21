package com.vljx.hawkspeed.ui.screens.authenticated.world

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition

sealed class WorldMapUiState {
    /**
     * The initial state to display the operation being performed throughout the loading/connecting process.
     */
    object Loading: WorldMapUiState()

    /**
     * The success state.
     */
    data class WorldMapLoaded(
        val playerUid: String,
        val gameSettings: GameSettings,
        val location: PlayerPosition,
        val approximateOnly: Boolean
    ): WorldMapUiState()

    /**
     * A failure state that reports a failure to organise the correct permissions or settings.
     */
    data class PermissionOrSettingsFailure(
        val permissionState: LocationPermissionState,
        val settingsState: LocationSettingsState
    ): WorldMapUiState()

    /**
     * The Player has configured their game's settings to forcefully avoid connecting to game server. They will need to adjust their settings
     * to be considered again. Or, the game server is currently not operating.
     */
    data class GameSettingsFailure(
        val gameSettings: GameSettings?
    ): WorldMapUiState()

    /**
     * A failure state that reports a failure to get location, or a lack of one. Providing an exception will specify that this is an issue.
     */
    data class NoLocation(
        val exception: Exception?
    ): WorldMapUiState()

    /**
     * A state that recognises that the socket is not yet connected, and so will pass the latest valid location for this intent.
     */
    data class NotConnected(
        val gameSettings: GameSettings,
        val location: PlayerPosition
    ): WorldMapUiState()

    /**
     * A failure state that reports a failure to connect to the server.
     */
    data class ConnectionFailure(
        val resourceError: ResourceError?
    ): WorldMapUiState()
}