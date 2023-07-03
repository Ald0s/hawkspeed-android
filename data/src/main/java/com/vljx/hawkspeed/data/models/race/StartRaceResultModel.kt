package com.vljx.hawkspeed.data.models.race

import com.vljx.hawkspeed.data.models.SocketErrorWrapperModel

data class StartRaceResultModel(
    val isStarted: Boolean,
    val race: RaceModel?,
    val error: SocketErrorWrapperModel?
)