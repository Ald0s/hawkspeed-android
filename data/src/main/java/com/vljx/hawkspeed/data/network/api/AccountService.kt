package com.vljx.hawkspeed.data.network.api

import com.vljx.hawkspeed.data.network.models.account.AccountDto
import com.vljx.hawkspeed.data.network.models.account.CheckNameDto
import com.vljx.hawkspeed.data.network.models.account.RegistrationDto
import com.vljx.hawkspeed.data.network.requestmodels.RequestRegisterLocalAccountDto
import com.vljx.hawkspeed.data.network.requestmodels.RequestSetupProfileDto
import retrofit2.Response
import retrofit2.http.*

interface AccountService {
    @POST("api/v1/auth")
    suspend fun authenticate(): Response<AccountDto>

    @POST("api/v1/auth")
    suspend fun authenticate(
        @Header("Authorization") authHeader: String
    ): Response<AccountDto>

    @POST("api/v1/logout")
    suspend fun logout(): Response<AccountDto>

    @POST("api/v1/register")
    @Headers("Content-Type: application/json")
    suspend fun registerLocalAccount(
        @Body requestRegisterLocalAccountDto: RequestRegisterLocalAccountDto
    ): Response<RegistrationDto>

    @POST("api/v1/setup/name/{userName}")
    suspend fun checkName(
        @Path("userName", encoded = true) userName: String
    ): Response<CheckNameDto>

    @POST("api/v1/setup")
    @Headers("Content-Type: application/json")
    suspend fun setupProfile(
        @Body setupAccountSocialRequestDto: RequestSetupProfileDto
    ): Response<AccountDto>
}