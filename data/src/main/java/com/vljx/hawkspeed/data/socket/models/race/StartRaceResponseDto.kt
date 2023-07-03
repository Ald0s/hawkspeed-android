package com.vljx.hawkspeed.data.socket.models.race

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.socket.models.SocketErrorWrapperDto

data class StartRaceResponseDto(
    @Expose
    @SerializedName("is_started")
    val isStarted: Boolean,

    @Expose
    @SerializedName("race")
    val race: RaceUpdateDto?,

    @Expose
    @SerializedName("exception")
    val error: SocketErrorWrapperDto?
)