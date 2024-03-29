package com.vljx.hawkspeed.ui.screens.authenticated.world

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation

sealed class WorldMapUiState {
    /**
     * The success state, indicating that the track recording mode should be used.
     */
    data class WorldMapLoadedRecordTrackMode(
        val playerUid: String,
        val account: Account,
        val gameSettings: GameSettings,
        val locationWithOrientation: PlayerPositionWithOrientation
    ): WorldMapUiState()

    /**
     * The success state, indicating that the race mode should be used.
     */
    data class WorldMapLoadedRaceMode(
        val playerUid: String,
        val account: Account,
        val gameSettings: GameSettings,
        val locationWithOrientation: PlayerPositionWithOrientation,
        val trackUid: String
    ): WorldMapUiState()

    /**
     * The success state, indicating that the standard mode should be used.
     */
    data class WorldMapLoadedStandardMode(
        val playerUid: String,
        val account: Account,
        val gameSettings: GameSettings,
        val locationWithOrientation: PlayerPositionWithOrientation,
        val approximateOnly: Boolean
    ): WorldMapUiState()

    /**
     * A failure state, indicating that the desired world action state for a particular reason. This should be implemented as a screen with a button
     * to revert back to standard mode, but in the interim lock the view and gray out the background.
     */
    data class NonStandardModeFailure(
        val requestedMode: WorldActionState,
        val reason: String
    ): WorldMapUiState() {
        companion object {
            const val MISSING_PRECISE_LOCATION_PERMISSION = "location-inaccurate"
        }
    }

    /**
     * The initial state to display the operation being performed throughout the loading/connecting process.
     */
    object Loading: WorldMapUiState()

    /**
     * A failure state that reports the current device has insufficient hardware to properly use HawkSpeed, and participate in the world.
     */
    data class DeviceSensorsIneptFailure(
        val sensorState: SensorState
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
        val locationWithOrientation: PlayerPositionWithOrientation
    ): WorldMapUiState()

    /**
     * A state that indicates a connection to the game server is in progress. If the resource error is not null, that means the error
     * provided is the reason for the current attempt at connecting. For example, lost connection to server.
     */
    data class Connecting(
        val resourceError: ResourceError? = null
    ): WorldMapUiState()

    /**
     * A failure state that reports a failure to connect to the server.
     */
    data class ConnectionFailure(
        val resourceError: ResourceError?
    ): WorldMapUiState()
}