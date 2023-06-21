package com.vljx.hawkspeed.data.socket.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.track.TrackDto

data class PlayerUpdateResponseDto(
    @Expose
    @SerializedName("player_uid")
    val playerUid: String,

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
    @SerializedName("world_object_update")
    val worldObjectUpdate: WorldObjectUpdateResponseDto?
)