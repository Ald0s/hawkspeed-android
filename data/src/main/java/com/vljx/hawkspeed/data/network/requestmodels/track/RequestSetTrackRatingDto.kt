package com.vljx.hawkspeed.data.network.requestmodels.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestSetTrackRatingDto(
    @Expose
    @SerializedName("rating")
    val rating: Boolean
)