package com.vljx.hawkspeed.domain.models.world

data class StartRaceResult(
    val isStarted: Boolean,
    val race: RaceUpdate?,
    val errorCode: String?
)