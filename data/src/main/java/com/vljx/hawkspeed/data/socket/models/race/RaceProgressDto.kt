package com.vljx.hawkspeed.data.socket.models.race

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RaceProgressDto(
    @Expose
    @SerializedName("race")
    val race: RaceUpdateDto
)