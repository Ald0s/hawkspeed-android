package com.vljx.hawkspeed.data.models.race

import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.data.models.vehicle.VehicleModel
import com.vljx.hawkspeed.domain.enums.TrackType

data class RaceLeaderboardModel(
    val raceUid: String,
    val finishingPlace: Int,
    val started: Long,
    val finished: Long,
    val stopwatch: Int,
    val averageSpeed: Int?,
    val percentMissed: Int,
    val player: UserModel,
    val vehicle: VehicleModel,
    val trackUid: String,
    val trackName: String,
    val trackType: TrackType
)