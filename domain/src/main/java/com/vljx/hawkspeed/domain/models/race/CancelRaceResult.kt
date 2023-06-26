package com.vljx.hawkspeed.domain.models.race

data class CancelRaceResult(
    val race: RaceUpdate?,
    val reasonCode: String?
)