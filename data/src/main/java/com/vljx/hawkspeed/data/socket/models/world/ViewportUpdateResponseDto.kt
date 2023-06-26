package com.vljx.hawkspeed.data.socket.models.world

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.track.TrackDto

data class ViewportUpdateResponseDto(
    @Expose
    @SerializedName("tracks")
    val tracks: List<TrackDto>
)