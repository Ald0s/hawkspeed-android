package com.vljx.hawkspeed.data.socket.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.track.TrackDto

data class WorldObjectUpdateResponseDto(
    @Expose
    @SerializedName("tracks")
    val tracks: List<TrackDto>
)