package com.vljx.hawkspeed.domain.models.race

data class Race(
    val raceUid: String,
    val trackUid: String,
    val started: Long,
    val finished: Long?,
    val isDisqualified: Boolean,
    val disqualificationReason: String?,
    val isCancelled: Boolean,
    val averageSpeed: Int?,
    val numLapsComplete: Int?,
    val percentComplete: Int?
) {
    val isFinished: Boolean
        get() = finished != null

    companion object {
        const val DQ_REASON_DISCONNECTED = "disconnected"
        const val DQ_REASON_MISSED_TRACK = "missed-track"
    }
}