package com.vljx.hawkspeed.domain.models.track

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