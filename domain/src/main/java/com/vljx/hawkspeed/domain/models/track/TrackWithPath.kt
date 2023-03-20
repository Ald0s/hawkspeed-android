package com.vljx.hawkspeed.domain.models.track

data class TrackWithPath(
    val track: Track,
    var path: TrackPath?
) {
    override fun equals(other: Any?): Boolean {
        return if(other is TrackWithPath) {
            other.track.trackUid == track.trackUid
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return track.trackUid.hashCode()
    }
}