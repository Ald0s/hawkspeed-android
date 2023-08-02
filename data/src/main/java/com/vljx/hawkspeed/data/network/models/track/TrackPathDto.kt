package com.vljx.hawkspeed.data.network.models.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TrackPathDto(
    @Expose
    @SerializedName("track_uid")
    val trackUid: String,

    @Expose
    @SerializedName("hash")
    val hash: String,

    @Expose
    @SerializedName("crs")
    val crs: Int,

    @Expose
    @SerializedName("points")
    val points: List<TrackPointDto>
)