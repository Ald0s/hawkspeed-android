package com.vljx.hawkspeed.data.models.race

import com.vljx.hawkspeed.data.models.user.UserModel

data class RaceLeaderboardModel(
    val raceUid: String,
    val finishingPlace: Int,
    val started: Long,
    val finished: Long,
    val stopwatch: Int,
    val player: UserModel,
    val trackUid: String
)