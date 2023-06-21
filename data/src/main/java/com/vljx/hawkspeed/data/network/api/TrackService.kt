package com.vljx.hawkspeed.data.network.api

import com.vljx.hawkspeed.data.network.models.race.RaceLeaderboardPageDto
import com.vljx.hawkspeed.data.network.models.track.TrackDto
import com.vljx.hawkspeed.data.network.models.track.TrackWithPathDto
import com.vljx.hawkspeed.data.network.requestmodels.track.SubmitTrackRequestDto
import retrofit2.Response
import retrofit2.http.*

interface TrackService {
    @GET("v1/track/{uidTrack}")
    suspend fun queryTrackByUid(
        @Path(value = "uidTrack", encoded = true) uidTrack: String
    ): Response<TrackDto>

    @GET("v1/track/{uidTrack}/path")
    suspend fun queryTrackWithPathByUid(
        @Path(value = "uidTrack", encoded = true) uidTrack: String
    ): Response<TrackWithPathDto>

    @GET("v1/track/{uidTrack}/leaderboard")
    suspend fun queryLeaderboardPage(
        @Path(value = "uidTrack", encoded = true) uidTrack: String,
        @Query("p") page: Int
    ): Response<RaceLeaderboardPageDto>

    @PUT("v1/track/new")
    @Headers("Content-Type: application/json")
    suspend fun submitNewTrack(
        @Body submitTrackRequestDto: SubmitTrackRequestDto
    ): Response<TrackDto>
}