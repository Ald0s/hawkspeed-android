package com.vljx.hawkspeed.data.network.models.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UserDto(
    @Expose
    @SerializedName("uid")
    val userUid: String,

    @Expose
    @SerializedName("username")
    val userName: String,

    @Expose
    @SerializedName("bio")
    val bio: String?,

    @Expose
    @SerializedName("privilege")
    val privilege: Int,

    @Expose
    @SerializedName("is_bot")
    val isBot: Boolean,

    @Expose
    @SerializedName("is_you")
    val isYou: Boolean
)