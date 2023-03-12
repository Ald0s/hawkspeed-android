package com.vljx.hawkspeed.data.network.models.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TrackPathDto(
    @Expose
    @SerializedName("uid")
    val trackUid: String,

    @Expose
    @SerializedName("points")
    val points: List<TrackPointDto>
)