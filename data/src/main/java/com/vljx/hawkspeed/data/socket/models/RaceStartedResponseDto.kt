package com.vljx.hawkspeed.data.socket.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RaceStartedResponseDto(
    @Expose
    @SerializedName("race_uid")
    val raceUid: String?,

    // TODO: disqualification nullables too
)