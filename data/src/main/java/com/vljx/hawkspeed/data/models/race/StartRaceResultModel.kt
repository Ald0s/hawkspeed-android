package com.vljx.hawkspeed.data.models.race

data class StartRaceResultModel(
    val isStarted: Boolean,
    val race: RaceUpdateModel?,
    val errorCode: String?
)