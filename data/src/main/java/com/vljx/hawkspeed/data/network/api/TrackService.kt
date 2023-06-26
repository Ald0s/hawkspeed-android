package com.vljx.hawkspeed.data.network.api

import com.vljx.hawkspeed.data.network.models.race.RaceLeaderboardPageDto
import com.vljx.hawkspeed.data.network.models.track.TrackCommentsPageDto
import com.vljx.hawkspeed.data.network.models.track.TrackDto
import com.vljx.hawkspeed.data.network.models.track.TrackWithPathDto
import com.vljx.hawkspeed.data.network.requestmodels.track.RequestSetTrackRatingDto
import com.vljx.hawkspeed.data.network.requestmodels.track.RequestSubmitTrackDto
import retrofit2.Response
import retrofit2.http.*

interface TrackService {
    @GET("api/v1/track/{uidTrack}")
    suspend fun queryTrackByUid(
        @Path(value = "uidTrack", encoded = true) uidTrack: String
    ): Response<TrackDto>

    @GET("api/v1/track/{uidTrack}/path")
    suspend fun queryTrackWithPathByUid(
        @Path(value = "uidTrack", encoded = true) uidTrack: String
    ): Response<TrackWithPathDto>

    @POST("api/v1/track/{uidTrack}/rate")
    @Headers("Content-Type: application/json")
    suspend fun setTrackRating(
        @Path(value = "uidTrack", encoded = true) uidTrack: String,
        @Body requestSetTrackRatingDto: RequestSetTrackRatingDto
    ): Response<TrackDto>

    @DELETE("api/v1/track/{uidTrack}/rate")
    suspend fun clearTrackRating(
        @Path(value = "uidTrack", encoded = true) uidTrack: String
    ): Response<TrackDto>

    @GET("api/v1/track/{uidTrack}/leaderboard")
    suspend fun queryLeaderboardPage(
        @Path(value = "uidTrack", encoded = true) uidTrack: String,
        @Query("p") page: Int
    ): Response<RaceLeaderboardPageDto>

    @GET("api/v1/track/{uidTrack}/comments")
    suspend fun queryCommentsPage(
        @Path(value = "uidTrack", encoded = true) uidTrack: String,
        @Query("p") page: Int
    ): Response<TrackCommentsPageDto>

    @PUT("api/v1/track/new")
    @Headers("Content-Type: application/json")
    suspend fun submitNewTrack(
        @Body requestSubmitTrackDto: RequestSubmitTrackDto
    ): Response<TrackDto>
}