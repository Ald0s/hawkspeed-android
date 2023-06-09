package com.vljx.hawkspeed.data.network.models.race

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.user.UserDto
import com.vljx.hawkspeed.data.network.models.vehicle.VehicleDto

data class RaceLeaderboardDto(
    @Expose
    @SerializedName("race_uid")
    val raceUid: String,

    @Expose
    @SerializedName("finishing_place")
    val finishingPlace: Int,

    @Expose
    @SerializedName("started")
    val started: Long,

    @Expose
    @SerializedName("finished")
    val finished: Long,

    @Expose
    @SerializedName("stopwatch")
    val stopwatch: Int,

    @Expose
    @SerializedName("player")
    val player: UserDto,

    @Expose
    @SerializedName("vehicle")
    val vehicle: VehicleDto,

    @Expose
    @SerializedName("track_uid")
    val trackUid: String
)