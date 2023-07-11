package com.vljx.hawkspeed.data.socket.models.world

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ConnectAuthenticationResponseDto(
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
    @SerializedName("bearing")
    val bearing: Float,

    @Expose
    @SerializedName("world_object_update")
    val worldObjectUpdate: WorldObjectUpdateResponseDto?
)