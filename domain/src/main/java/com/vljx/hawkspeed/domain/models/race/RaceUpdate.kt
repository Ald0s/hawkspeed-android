package com.vljx.hawkspeed.domain.models.race

data class RaceUpdate(
    val raceUid: String,
    val trackUid: String,
    val started: Long,
    val finished: Long?,
    val isDisqualified: Boolean,
    // TODO dq_extra_info
    val disqualificationReason: String?,
    val isCancelled: Boolean
)