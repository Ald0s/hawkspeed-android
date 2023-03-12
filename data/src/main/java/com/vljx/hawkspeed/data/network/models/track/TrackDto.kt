package com.vljx.hawkspeed.data.network.models.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.user.UserDto

data class TrackDto(
    @Expose
    @SerializedName("uid")
    val trackUid: String,

    @Expose
    @SerializedName("name")
    val name: String,

    @Expose
    @SerializedName("description")
    val description: String,

    @Expose
    @SerializedName("owner")
    val owner: UserDto,

    @Expose
    @SerializedName("start_point")
    val startPoint: TrackPointDto,

    @Expose
    @SerializedName("verified")
    val isVerified: Boolean,

    @Expose
    @SerializedName("can_race")
    val canRace: Boolean,

    @Expose
    @SerializedName("can_edit")
    val canEdit: Boolean,

    @Expose
    @SerializedName("can_delete")
    val canDelete: Boolean,

    //@Expose
    //@SerializedName("points")
    //val points: List<TrackPointDto>
)