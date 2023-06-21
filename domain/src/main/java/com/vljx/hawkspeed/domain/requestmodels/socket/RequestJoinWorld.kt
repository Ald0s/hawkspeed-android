package com.vljx.hawkspeed.domain.requestmodels.socket

import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.models.world.PlayerPosition

data class RequestJoinWorld(
    val gameSettings: GameSettings,
    val location: PlayerPosition
)