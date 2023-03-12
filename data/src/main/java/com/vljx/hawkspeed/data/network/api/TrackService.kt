package com.vljx.hawkspeed.data.network.api

import com.vljx.hawkspeed.data.network.models.track.TrackDto
import com.vljx.hawkspeed.data.network.models.track.TrackPathDto
import com.vljx.hawkspeed.data.network.requests.track.SubmitTrackRequestDto
import retrofit2.Response
import retrofit2.http.*

interface TrackService {
    @GET("v1/track/{uidTrack}")
    suspend fun queryTrackByUid(
        @Path(value = "uidTrack", encoded = true) uidTrack: String
    ): Response<TrackDto>

    @GET("v1/track/{uidTrack}/path")
    suspend fun queryTrackPathByUid(
        @Path(value = "uidTrack", encoded = true) uidTrack: String
    ): Response<TrackPathDto>

    @PUT("v1/track/new")
    @Headers("Content-Type: application/json")
    suspend fun submitNewTrack(
        @Body submitTrackRequestDto: SubmitTrackRequestDto
    ): Response<TrackDto>
}