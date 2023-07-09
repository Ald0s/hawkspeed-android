package com.vljx.hawkspeed.domain.exc.race

class NoTrackPathException: Exception(NO_TRACK_PATH) {
    companion object {
        const val NO_TRACK_PATH = "no-track-path"
    }
}