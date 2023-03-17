package com.vljx.hawkspeed.domain.models.race

data class Race(
    val raceUid: String,
    val trackUid: String,
    val started: Long,
    val finished: Long?,
    val isDisqualified: Boolean,
    val disqualificationReason: String?,
    val isCancelled: Boolean
) {
    val isFinished: Boolean
        get() = finished != null
}