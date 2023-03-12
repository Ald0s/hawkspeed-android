package com.vljx.hawkspeed.data.source

import com.vljx.hawkspeed.data.models.account.AccountModel
import com.vljx.hawkspeed.data.models.account.CheckNameModel
import com.vljx.hawkspeed.data.models.account.RegistrationModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requests.CheckNameRequest
import com.vljx.hawkspeed.domain.requests.RegisterLocalAccountRequest
import com.vljx.hawkspeed.domain.requests.SetupProfileRequest

interface AccountRemoteData {
    suspend fun checkUsernameTaken(checkNameRequest: CheckNameRequest): Resource<CheckNameModel>
    suspend fun setupProfile(setupProfileRequest: SetupProfileRequest): Resource<AccountModel>

    suspend fun attemptAuthentication(): Resource<AccountModel>
    suspend fun attemptAuthentication(
        emailAddress: String,
        password: String
    ): Resource<AccountModel>

    suspend fun logout(): Resource<AccountModel>

    suspend fun registerLocalAccount(registerLocalAccountRequest: RegisterLocalAccountRequest): Resource<RegistrationModel>
}