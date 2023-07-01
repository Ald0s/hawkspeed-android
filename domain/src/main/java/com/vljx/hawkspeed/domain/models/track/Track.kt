package com.vljx.hawkspeed.domain.models.track

import android.location.Location
import android.os.Parcelable
import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.user.User
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val trackUid: String,
    val name: String,
    val description: String,
    val owner: User,
    val topLeaderboard: List<RaceLeaderboard>,
    val startPoint: TrackPoint,
    val isVerified: Boolean,
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
     * Returns true if the rotation given means that the Player is oriented in the correct direction for this track.
     * TODO: complete this, for now, it will always return true.
     */
    fun isOrientationCorrectFor(rotation: Float): Boolean {
        // TODO: check that bearing is appropriate. Player should be facing the same direction.
        return true
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