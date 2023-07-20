package com.vljx.hawkspeed.data.network.models.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.user.UserDto

data class TrackCommentDto(
    @Expose
    @SerializedName("uid")
    val commentUid: String,

    @Expose
    @SerializedName("created")
    val created: Int,

    @Expose
    @SerializedName("text")
    val text: String,

    @Expose
    @SerializedName("user")
    val user: UserDto,

    @Expose
    @SerializedName("track_uid")
    val trackUid: String
)