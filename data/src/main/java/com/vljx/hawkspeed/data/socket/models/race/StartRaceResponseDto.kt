package com.vljx.hawkspeed.data.socket.models.race

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class StartRaceResponseDto(
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