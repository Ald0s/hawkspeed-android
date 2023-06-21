package com.vljx.hawkspeed.data.network.api

import com.vljx.hawkspeed.data.network.models.account.AccountDto
import com.vljx.hawkspeed.data.network.models.account.CheckNameDto
import com.vljx.hawkspeed.data.network.models.account.RegistrationDto
import com.vljx.hawkspeed.data.network.requestmodels.RequestRegisterLocalAccountDto
import com.vljx.hawkspeed.data.network.requestmodels.RequestSetupProfileDto
import retrofit2.Response
import retrofit2.http.*

interface AccountService {
    @POST("v1/auth")
    suspend fun authenticate(): Response<AccountDto>

    @POST("v1/auth")
    suspend fun authenticate(
        @Header("Authorization") authHeader: String
    ): Response<AccountDto>

    @POST("v1/logout")
    suspend fun logout(): Response<AccountDto>

    @POST("v1/register")
    @Headers("Content-Type: application/json")
    suspend fun registerLocalAccount(
        @Body requestRegisterLocalAccountDto: RequestRegisterLocalAccountDto
    ): Response<RegistrationDto>

    @POST("v1/setup/name/{userName}")
    suspend fun checkName(
        @Path("userName", encoded = true) userName: String
    ): Response<CheckNameDto>

    @POST("v1/setup")
    @Headers("Content-Type: application/json")
    suspend fun setupProfile(
        @Body setupAccountSocialRequestDto: RequestSetupProfileDto
    ): Response<AccountDto>
}