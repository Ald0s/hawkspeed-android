package com.vljx.hawkspeed.data.network.requestmodels.track

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack

data class RequestSubmitTrackDto(
    @Expose
    @SerializedName("name")
    val name: String,

    @Expose
    @SerializedName("description")
    val description: String,

    @Expose
    @SerializedName("points")
    val points: List<RequestSubmitTrackPointDto>
) {
    constructor(requestSubmitTrack: RequestSubmitTrack):
            this(
                requestSubmitTrack.name,
                requestSubmitTrack.description,
                requestSubmitTrack.points.map {
                    RequestSubmitTrackPointDto(it)
                }
            )
}