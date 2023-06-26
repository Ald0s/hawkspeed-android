package com.vljx.hawkspeed.domain.models.track

import com.vljx.hawkspeed.domain.models.world.PlayerPosition

data class TrackDraftWithPoints(
    val trackDraftId: Long,
    val trackName: String?,
    val trackDescription: String?,
    val pointDrafts: List<TrackPointDraft>
) {
    /**
     * A property that will return true if this draft has a proper track recorded.
     * For now, this is any track where point drafts is greater than 0 items in length.
     */
    val hasRecordedTrack: Boolean
        get() = pointDrafts.isNotEmpty()

    /**
     * A property that will return the very first point, or null if none.
     */
    val firstPointDraft: TrackPointDraft?
        get() = pointDrafts.firstOrNull()

    /**
     * A property that will return the very last point, or null if none.
     */
    val lastPointDraft: TrackPointDraft?
        get() = pointDrafts.lastOrNull()

    /**
     * A function that will determine whether the given player position should be added as a new point to this track, considering the last point
     * added. TODO: finish this function, for now, it will always return true.
     */
    fun shouldTakePosition(location: PlayerPosition): Boolean {
        return true
    }
}