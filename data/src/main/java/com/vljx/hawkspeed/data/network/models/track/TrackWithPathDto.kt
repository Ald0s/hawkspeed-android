package com.vljx.hawkspeed.data.network.models.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TrackWithPathDto(
    @Expose
    @SerializedName("track")
    val track: TrackDto,

    @Expose
    @SerializedName("track_path")
    val trackPath: TrackPathDto
)