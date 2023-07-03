package com.vljx.hawkspeed.domain.models.race

import com.vljx.hawkspeed.domain.ResourceError

data class StartRaceResult(
    val isStarted: Boolean,
    val race: Race?,
    val socketError: ResourceError.SocketError?
)