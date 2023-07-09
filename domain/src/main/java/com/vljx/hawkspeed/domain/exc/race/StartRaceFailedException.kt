package com.vljx.hawkspeed.domain.exc.race

import com.vljx.hawkspeed.domain.ResourceError

class StartRaceFailedException(
    val socketError: ResourceError.SocketError?
): Exception() {
    companion object {
        const val REASON_ALREADY_IN_RACE = "already-in-race"
        const val REASON_POSITION_NOT_SUPPORTED = "position-not-supported"
        const val REASON_NO_COUNTDOWN_POSITION = "no-countdown-position"
        const val REASON_NO_STARTED_POSITION = "no-started-position"
        const val REASON_NO_TRACK_FOUND = "no-track-found"
        const val REASON_TRACK_NOT_READY = "cant-be-raced"
        const val REASON_NO_VEHICLE_UID = "no-vehicle-uid"
        const val REASON_NO_VEHICLE = "no-vehicle"
    }
}