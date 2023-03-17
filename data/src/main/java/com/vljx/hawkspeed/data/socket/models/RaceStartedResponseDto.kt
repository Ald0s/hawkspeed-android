package com.vljx.hawkspeed.data.socket.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.race.RaceUpdateDto

data class RaceStartedResponseDto(
    @Expose
    @SerializedName("is_started")
    val isStarted: Boolean,

    @Expose
    @SerializedName("race")
    val race: RaceUpdateDto?,

    @Expose
    @SerializedName("error_code")
    val errorCode: String?
)