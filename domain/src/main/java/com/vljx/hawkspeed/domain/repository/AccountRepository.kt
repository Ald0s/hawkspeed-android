package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.account.CheckName
import com.vljx.hawkspeed.domain.models.account.Registration
import com.vljx.hawkspeed.domain.requests.CheckNameRequest
import com.vljx.hawkspeed.domain.requests.LoginRequest
import com.vljx.hawkspeed.domain.requests.RegisterLocalAccountRequest
import com.vljx.hawkspeed.domain.requests.SetupProfileRequest
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    /**
     * Get a flow for an instance of Account.
     */
    fun getAccountByUid(userUid: String): Flow<Resource<Account>>

    suspend fun checkUsernameTaken(checkNameRequest: CheckNameRequest): Flow<Resource<CheckName>>
    suspend fun setupProfile(setupProfileRequest: SetupProfileRequest): Flow<Resource<Account>>

    /**
     * Attempt authentication without any parameters - this will attempt to authenticate the current login token stored in cookies. If this function
     * fails, this means that a login is required.
     */
    suspend fun attemptAuthentication(): Flow<Resource<Account>>

    /**
     * Attempt authentication with normal login parameters.
     */
    suspend fun attemptAuthentication(loginRequest: LoginRequest): Flow<Resource<Account>>

    /**
     * Logout from the current account.
     */
    suspend fun logout(): Flow<Resource<Account>>

    suspend fun registerLocalAccount(params: RegisterLocalAccountRequest): Flow<Resource<Registration>>
}