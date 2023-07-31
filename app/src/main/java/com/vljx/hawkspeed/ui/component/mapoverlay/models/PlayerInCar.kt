package com.vljx.hawkspeed.ui.component.mapoverlay.models

import com.vljx.hawkspeed.domain.models.world.PlayerPosition

/**
 * A data class that associates a player's position with a loaded car.
 */
data class PlayerInCar(
    val playerPosition: PlayerPosition,
    val car: Car
)