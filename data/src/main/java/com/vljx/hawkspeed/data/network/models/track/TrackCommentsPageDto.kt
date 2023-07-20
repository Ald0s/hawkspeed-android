package com.vljx.hawkspeed.data.network.models.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.base.Paged

data class TrackCommentsPageDto(
    @Expose
    @SerializedName("track")
    val track: TrackDto,

    @Expose
    @SerializedName("items")
    val comments: List<TrackCommentDto>,

    @Expose
    @SerializedName("this_page")
    override val thisPage: Int,

    @Expose
    @SerializedName("next_page")
    override val nextPage: Int?
): Paged