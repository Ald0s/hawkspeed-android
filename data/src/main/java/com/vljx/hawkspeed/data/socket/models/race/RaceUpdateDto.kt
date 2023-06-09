package com.vljx.hawkspeed.data.socket.models.race

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RaceUpdateDto(
    @Expose
    @SerializedName("uid")
    val raceUid: String,

    @Expose
    @SerializedName("track_uid")
    val trackUid: String,

    @Expose
    @SerializedName("started")
    val started: Long,

    @Expose
    @SerializedName("finished")
    val finished: Long?,

    @Expose
    @SerializedName("disqualified")
    val isDisqualified: Boolean,

    // TODO dq_extra_info

    @Expose
    @SerializedName("dq_reason")
    val disqualificationReason: String?,

    @Expose
    @SerializedName("cancelled")
    val isCancelled: Boolean,

    @Expose
    @SerializedName("average_speed")
    val averageSpeed: Int?,

    @Expose
    @SerializedName("num_laps_complete")
    val numLapsComplete: Int?,

    @Expose
    @SerializedName("percent_complete")
    val percentComplete: Int?
)