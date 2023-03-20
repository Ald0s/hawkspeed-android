package com.vljx.hawkspeed.domain.models.track

import android.location.Location
import android.os.Parcelable
import com.vljx.hawkspeed.domain.models.user.User
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Track(
    val trackUid: String,
    val name: String,
    val description: String,
    val owner: User,
    val startPoint: TrackPoint,
    val isVerified: Boolean,
    val canRace: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    //val points: List<TrackPoint>
): Parcelable {
    /**
     * Determines whether the Player referred to by the latitude, longitude and the rotation is situated such that they can
     * race this track.
     */
    fun canBeRacedBy(latitude: Double, longitude: Double, rotation: Float): Boolean {
        // Get the distance.
        val distanceResultArray = FloatArray(5)
        Location.distanceBetween(
            startPoint.latitude,
            startPoint.longitude,
            latitude,
            longitude,
            distanceResultArray
        )
        // If the distance is equal to, less than 10, return the track.
        // TODO: check that bearing is appropriate. Player should be facing the same direction.
        if(distanceResultArray[0] <= 10f) {
            return true
        }
        return false
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