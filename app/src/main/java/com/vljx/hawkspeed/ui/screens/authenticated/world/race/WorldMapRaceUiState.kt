package com.vljx.hawkspeed.ui.screens.authenticated.world.race

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.race.Race
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation

sealed class WorldMapRaceUiState {
    /**
     * The finished state; the race in question has been successfully finished!
     */
    data class Finished(
        val race: Race,
        val raceLeaderboard: RaceLeaderboard,
        val track: Track,
        val trackPath: TrackPath
    ): WorldMapRaceUiState()

    /**
     * The cancelled state; the race in question has been cancelled. If the race instance is not null, this means the race was cancelled mid race. Otherwise, the race
     * was cancelled while countdown was progressing.
     */
    data class Cancelled(
        val race: Race?,
        val track: Track,
        val trackPath: TrackPath
    ): WorldMapRaceUiState()

    /**
     * The disqualified state; the race in question has been disqualified.
     */
    data class Disqualified(
        val race: Race,
        val track: Track,
        val trackPath: TrackPath
    ): WorldMapRaceUiState()

    /**
     * A state that indicates there is an ongoing race for the desired track. The associated UI should following the User, updating with their progress
     * from stopwatch, checkpoints, polyline consumed etc.
     */
    data class Racing(
        val race: Race,
        val track: Track,
        val trackPath: TrackPath
    ): WorldMapRaceUiState()

    /**
     * A state that indicates the race is now counting down. At this point, the User should be unable to start a new race, but should be able to
     * cancel the countdown for this race. No race has actually been created just yet.
     */
    data class CountingDown(
        /**
         * 0 - Get Ready!
         * 1 - 3
         * 2 - 2
         * 3 - 1
         * 4 - GO!
         */
        val currentSecond: Int = 0,
        val countdownStartedLocationWithOrientation: PlayerPositionWithOrientation,
        val track: Track,
        val trackPath: TrackPath
    ): WorldMapRaceUiState()

    /**
     * There is no ongoing race for the desired track but the User is close enough to request a race for it. This will just provide the track
     * and its path, to show the user interface associated with beginning the race, or viewing history or the track's detail etc.
     */
    data class OnStartLine(
        val yourVehicles: List<Vehicle>,
        val startLineState: StartLineState,
        val track: Track,
        val trackPath: TrackPath
    ): WorldMapRaceUiState()

    /**
     * The initial Loading state.
     */
    object Loading: WorldMapRaceUiState()

    /**
     * A state that communicates the race has failed to start due to server refusal or other similar types of failures.
     */
    data class RaceStartFailed(
        val reasonCode: String,
        val resourceError: ResourceError?
    ): WorldMapRaceUiState()

    /**
     * The initial load failure state. This can contain: API errors, general exceptions.
     */
    data class LoadFailed(
        val resourceError: ResourceError
    ): WorldMapRaceUiState()
}