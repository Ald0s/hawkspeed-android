package com.vljx.hawkspeed.domain.models.world

import com.vljx.hawkspeed.domain.models.account.Account

/**
 * A data class for containing all information and data related to the current User/Player and their game settings.
 */
data class CurrentPlayer(
    val account: Account,
    val gameSettings: GameSettings,
    val playerPosition: PlayerPosition
)