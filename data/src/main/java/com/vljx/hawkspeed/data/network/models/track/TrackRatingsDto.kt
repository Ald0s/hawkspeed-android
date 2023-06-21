package com.vljx.hawkspeed.data.network.models.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TrackRatingsDto(
    @Expose
    @SerializedName("num_positive_votes")
    val numPositiveVotes: Int,

    @Expose
    @SerializedName("num_negative_votes")
    val numNegativeVotes: Int
)