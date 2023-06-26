package com.vljx.hawkspeed.domain.models.race

data class StartRaceResult(
    val isStarted: Boolean,
    val race: RaceUpdate?,
    val errorCode: String?
)