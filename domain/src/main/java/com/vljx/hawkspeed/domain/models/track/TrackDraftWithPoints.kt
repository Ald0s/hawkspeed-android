package com.vljx.hawkspeed.domain.models.track

import android.location.Location
import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.world.BoundingBox
import com.vljx.hawkspeed.domain.models.world.Coordinate
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation
import com.vljx.hawkspeed.domain.thirdparty.BoundingBoxUtil
import java.lang.IndexOutOfBoundsException

data class TrackDraftWithPoints(
    val trackDraftId: Long,
    val trackType: TrackType,
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
     * A function that will return a bounding box containing the entire recorded track. If there are no points in the track, this function will
     * throw an out of bounds exception.
     */
    fun getBoundingBox(): BoundingBox {
        if(!hasRecordedTrack) {
            throw IndexOutOfBoundsException()
        }
        return BoundingBoxUtil.boundingBoxFrom(
            pointDrafts.map { Coordinate(it.latitude, it.longitude) }
        )
    }

    /**
     * A function that will determine whether the given player position should be added as a new point to this track, considering the last point
     * added.
     */
    fun shouldTakePosition(locationWithOrientation: PlayerPositionWithOrientation): Boolean {
        // Return false if there is under 5 meters between given location and latest point.
        val lastPoint = lastPointDraft
        if(lastPoint != null) {
            val results = FloatArray(5)

            Location.distanceBetween(
                locationWithOrientation.position.latitude,
                locationWithOrientation.position.longitude,
                lastPoint.latitude,
                lastPoint.longitude,
                results
            )

            if(results[0] >= 5) {
                return true
            }
            return false
        }
        // If there is no last point, always return true.
        return true
    }
}