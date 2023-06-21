package com.vljx.hawkspeed.data.socket.requestmodels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.requestmodels.race.RequestStartRace

data class RequestStartRaceDto(
    @Expose
    @SerializedName("track_uid")
    val trackUid: String,

    @Expose
    @SerializedName("started_position")
    val startedPosition: RequestPlayerLocationDto,

    @Expose
    @SerializedName("countdown_position")
    val countdownPosition: RequestPlayerLocationDto
) {
    constructor(requestStartRace: RequestStartRace) : this(
        requestStartRace.trackUid,
        RequestPlayerLocationDto(requestStartRace.startedPosition),
        RequestPlayerLocationDto(requestStartRace.countdownPosition)
    )
}