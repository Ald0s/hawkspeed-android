package com.vljx.hawkspeed.domain.models.world

import com.vljx.hawkspeed.domain.models.track.TrackWithPath

/**
 * A domain model for containing all world objects that should be drawn to the map.
 */
data class WorldObjects(
    val tracks: List<TrackWithPath>,
    val isEmpty: Boolean = false
) {
    fun report(): String =
        when(isEmpty) {
            true -> "== EMPTY WORLD OBJECTS =="
            else -> StringBuilder()
                .appendLine("== World Objects ==")
                .appendLine("# Tracks: ${tracks.size}")
                .toString()
        }
    companion object {
        fun empty(): WorldObjects =
            WorldObjects(
                listOf(),
                isEmpty = true
            )
    }
}