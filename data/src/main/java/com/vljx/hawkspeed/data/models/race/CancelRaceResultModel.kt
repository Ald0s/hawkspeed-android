package com.vljx.hawkspeed.data.models.race

data class CancelRaceResultModel(
    val race: RaceModel?,
    val cancellationReason: String?
)