package com.vljx.hawkspeed.domain.models.track

import android.location.Location
import android.os.Parcelable
import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.user.User
import kotlinx.parcelize.Parcelize
import kotlin.math.abs

@Parcelize
data class Track(
    val trackUid: String,
    val name: String,
    val description: String,
    val owner: User,
    val topLeaderboard: List<RaceLeaderboard>,
    val startPoint: TrackPoint,
    val startPointBearing: Float,
    val isVerified: Boolean,
    val length: Int,
    val isSnappedToRoads: Boolean,
    val trackType: TrackType,
    val numPositiveVotes: Int,
    val numNegativeVotes: Int,
    val yourRating: Boolean?,
    val numComments: Int,
    val canRace: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canComment: Boolean
): Parcelable {
    /**
     * A get property for the total length of this track, but formatted. Either displayed as meters (843m) or kilometers (3.2km)
     */
    val lengthFormatted: String
        get() = if(length < 1000) {
            "${length}m"
        } else {
            String.format("%.1fkm", length / 1000f)
        }

    /**
     * Returns the distance, in meters, between the given latitude and longitude, and the start poiint for this track.
     */
    fun distanceToStartPointFor(latitude: Double, longitude: Double): Float {
        // Get the distance.
        val distanceResultArray = FloatArray(5)
        Location.distanceBetween(
            startPoint.latitude,
            startPoint.longitude,
            latitude,
            longitude,
            distanceResultArray
        )
        return distanceResultArray[0]
    }

    /**
     * Returns true if the rotation given means that the Player is oriented in the correct direction for this track. We will check for an orientation that is similar
     * within 20 degrees in either direction to the start point bearing.
     */
    fun isOrientationCorrectFor(rotation: Float): Boolean {
        return abs(rotation) > startPointBearing - 20 && abs(rotation) < startPointBearing + 20
    }

    override fun equals(other: Any?): Boolean {
        return if(other is Track) {
            other.trackUid == trackUid
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return trackUid.hashCode()
    }

    companion object {
        const val ARG_TRACK = "com.vljx.hawkspeed.domain.models.track.Track.ARG_TRACK"
    }
}