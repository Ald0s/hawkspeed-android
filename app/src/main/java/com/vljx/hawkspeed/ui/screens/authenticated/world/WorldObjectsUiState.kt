package com.vljx.hawkspeed.ui.screens.authenticated.world

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.models.world.CurrentPlayer

sealed class WorldObjectsUiState {
    /**
     * A snapshot of the current world objects returned.
     */
    data class CurrentWorldObjects(
        val currentPlayer: CurrentPlayer,
        val tracks: SnapshotStateList<TrackWithPath>
    ): WorldObjectsUiState()

    /**
     * The default loading state for world objects.
     */
    object Loading: WorldObjectsUiState()
}