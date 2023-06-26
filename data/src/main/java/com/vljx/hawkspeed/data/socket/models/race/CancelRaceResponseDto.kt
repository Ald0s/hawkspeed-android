package com.vljx.hawkspeed.data.socket.models.race

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CancelRaceResponseDto(
    @Expose
    @SerializedName("race")
    val race: RaceUpdateDto?,

    @Expose
    @SerializedName("reason_code")
    val reasonCode: String?
)