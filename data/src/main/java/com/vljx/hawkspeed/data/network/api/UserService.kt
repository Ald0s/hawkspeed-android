package com.vljx.hawkspeed.data.network.api

import com.vljx.hawkspeed.data.network.models.user.UserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
    @GET("api/v1/user/{uidUser}")
    suspend fun queryUserByUid(
        @Path(value = "uidUser", encoded = true) uidUser: String
    ): Response<UserDto>
}