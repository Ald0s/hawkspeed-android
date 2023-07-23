package com.vljx.hawkspeed.data.network.models.race

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.user.UserDto
import com.vljx.hawkspeed.data.network.models.vehicle.VehicleDto
import com.vljx.hawkspeed.domain.enums.TrackType

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
    @SerializedName("average_speed")
    val averageSpeed: Int?,

    @Expose
    @SerializedName("percent_missed")
    val percentMissed: Int,

    @Expose
    @SerializedName("player")
    val player: UserDto,

    @Expose
    @SerializedName("vehicle")
    val vehicle: VehicleDto,

    @Expose
    @SerializedName("track_uid")
    val trackUid: String,

    @Expose
    @SerializedName("track_name")
    val trackName: String,

    @Expose
    @SerializedName("track_type")
    val trackType: TrackType
)