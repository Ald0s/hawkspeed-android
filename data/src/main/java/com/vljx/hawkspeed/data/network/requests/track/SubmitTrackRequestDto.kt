package com.vljx.hawkspeed.data.network.requests.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.requests.SubmitTrackRequest

data class SubmitTrackRequestDto(
    @Expose
    @SerializedName("name")
    val name: String,

    @Expose
    @SerializedName("description")
    val description: String,

    @Expose
    @SerializedName("points")
    val points: List<SubmitTrackPointRequestDto>
) {
    constructor(submitTrackRequest: SubmitTrackRequest):
            this(
                submitTrackRequest.name,
                submitTrackRequest.description,
                submitTrackRequest.points.map {
                    SubmitTrackPointRequestDto(it)
                }
            )
}