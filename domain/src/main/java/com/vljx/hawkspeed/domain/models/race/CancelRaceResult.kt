package com.vljx.hawkspeed.domain.models.race

data class CancelRaceResult(
    val race: Race?,
    val cancellationReason: String?
)