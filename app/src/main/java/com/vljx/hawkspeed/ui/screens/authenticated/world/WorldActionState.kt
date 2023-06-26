package com.vljx.hawkspeed.ui.screens.authenticated.world

/**
 * A state class that will communicate to the world map view the current action being requested by the User.
 */
sealed class WorldActionState {
    /**
     * The standard world map mode. The camera is in free view, all objects can be downloaded and viewed.
     */
    object StandardMode: WorldActionState()

    /**
     * The race mode. The camera is locked to follow the Player from behind, only objects related to the race are displayed. As well, this should
     * cause the drawing of race related user interfaces over the map.
     */
    data class RaceMode(
        val trackUid: String,
        val raceUid: String
    ): WorldActionState()

    /**
     * The track creation mode. The camera is locked to follow the Player from behind, only objects related to race recording are displayed. This should
     * cause the drawing of the race recording user interfaces over the map.
     */
    data class RecordTrackMode(
        val trackDraftId: Long
    ): WorldActionState()
}