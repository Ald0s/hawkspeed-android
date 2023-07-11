package com.vljx.hawkspeed.data.network.api

import com.vljx.hawkspeed.data.network.models.race.RaceLeaderboardDto
import com.vljx.hawkspeed.data.network.models.race.RaceLeaderboardPageDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RaceService {
    @GET("api/v1/track/{uidTrack}/leaderboard")
    suspend fun queryLeaderboardPage(
        @Path(value = "uidTrack", encoded = true) uidTrack: String,
        @Query("p") page: Int
    ): Response<RaceLeaderboardPageDto>

    @GET("api/v1/races/{uidRace}")
    suspend fun queryRace(
        @Path(value = "uidRace", encoded = true) uidRace: String
    )

    @GET("api/v1/races/{uidRace}/leaderboard")
    suspend fun queryRaceLeaderboardEntry(
        @Path(value = "uidRace", encoded = true) uidRace: String
    ): Response<RaceLeaderboardDto>
}