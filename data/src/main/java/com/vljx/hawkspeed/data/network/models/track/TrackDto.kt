package com.vljx.hawkspeed.data.network.models.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.race.RaceLeaderboardDto
import com.vljx.hawkspeed.data.network.models.user.UserDto
import com.vljx.hawkspeed.domain.enums.TrackType

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
    @SerializedName("top_leaderboard")
    val topLeaderboard: List<RaceLeaderboardDto>,

    @Expose
    @SerializedName("start_point")
    val startPoint: TrackPointDto,

    @Expose
    @SerializedName("is_verified")
    val isVerified: Boolean,

    @Expose
    @SerializedName("is_snapped_to_roads")
    val isSnappedToRoads: Boolean,

    @Expose
    @SerializedName("track_type")
    val trackType: TrackType,

    @Expose
    @SerializedName("ratings")
    val ratings: TrackRatingsDto,

    @Expose
    @SerializedName("your_rating")
    val yourRating: Boolean?,

    @Expose
    @SerializedName("num_comments")
    val numComments: Int,

    @Expose
    @SerializedName("can_race")
    val canRace: Boolean,

    @Expose
    @SerializedName("can_edit")
    val canEdit: Boolean,

    @Expose
    @SerializedName("can_delete")
    val canDelete: Boolean,

    @Expose
    @SerializedName("can_comment")
    val canComment: Boolean
)