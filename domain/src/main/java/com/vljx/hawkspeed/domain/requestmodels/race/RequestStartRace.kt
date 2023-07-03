package com.vljx.hawkspeed.domain.requestmodels.race

import com.vljx.hawkspeed.domain.requestmodels.socket.RequestPlayerUpdate

data class RequestStartRace(
    val trackUid: String,
    val vehicleUid: String?,
    val startedPosition: RequestPlayerUpdate,
    val countdownPosition: RequestPlayerUpdate
)