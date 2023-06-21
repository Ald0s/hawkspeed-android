package com.vljx.hawkspeed.ui.screens.authenticated.world

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.vljx.hawkspeed.domain.models.track.TrackWithPath

sealed class WorldObjectsUiState {
    object Loading: WorldObjectsUiState()

    data class GotWorldObjects(
        val tracks: SnapshotStateList<TrackWithPath>
    ): WorldObjectsUiState()
}