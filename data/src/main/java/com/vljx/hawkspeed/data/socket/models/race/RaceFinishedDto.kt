package com.vljx.hawkspeed.data.socket.models.race

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.race.RaceLeaderboardDto

class RaceFinishedDto(
    @Expose
    @SerializedName("race")
    val race: RaceUpdateDto
)