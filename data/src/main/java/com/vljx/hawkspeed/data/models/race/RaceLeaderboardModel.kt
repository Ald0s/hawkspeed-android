package com.vljx.hawkspeed.data.models.race

import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.data.models.vehicle.VehicleModel

data class RaceLeaderboardModel(
    val raceUid: String,
    val finishingPlace: Int,
    val started: Long,
    val finished: Long,
    val stopwatch: Int,
    val player: UserModel,
    val vehicle: VehicleModel,
    val trackUid: String
)