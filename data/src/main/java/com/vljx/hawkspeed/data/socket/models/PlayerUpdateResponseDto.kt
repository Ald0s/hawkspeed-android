package com.vljx.hawkspeed.data.socket.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.track.TrackDto

data class PlayerUpdateResponseDto(
    @Expose
    @SerializedName("latitude")
    val latitude: Double,

    @Expose
    @SerializedName("longitude")
    val longitude: Double,

    @Expose
    @SerializedName("rotation")
    val rotation: Double,

    @Expose
    @SerializedName("viewport_update")
    val viewportUpdate: ViewportUpdateResponseDto?
)