package com.vljx.hawkspeed.data.models.race

data class RaceModel(
    val raceUid: String,
    val trackUid: String,
    val started: Long,
    val finished: Long?,
    val isDisqualified: Boolean,
    val disqualificationReason: String?,
    val isCancelled: Boolean
)