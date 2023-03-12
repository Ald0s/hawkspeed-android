package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.account.AccountModel
import com.vljx.hawkspeed.data.models.account.CheckNameModel
import com.vljx.hawkspeed.data.models.account.RegistrationModel
import com.vljx.hawkspeed.data.network.api.AccountService
import com.vljx.hawkspeed.data.network.mapper.account.AccountDtoMapper
import com.vljx.hawkspeed.data.network.mapper.account.CheckNameDtoMapper
import com.vljx.hawkspeed.data.network.mapper.account.RegistrationDtoMapper
import com.vljx.hawkspeed.data.network.requests.RegisterLocalAccountRequestDto
import com.vljx.hawkspeed.data.network.requests.SetupProfileRequestDto
import com.vljx.hawkspeed.data.source.AccountRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requests.CheckNameRequest
import com.vljx.hawkspeed.domain.requests.RegisterLocalAccountRequest
import com.vljx.hawkspeed.domain.requests.SetupProfileRequest
import okhttp3.Credentials
import javax.inject.Inject

class AccountRemoteDataImpl @Inject constructor(
    private val accountService: AccountService,

    private val accountDtoMapper: AccountDtoMapper,
    private val checkNameDtoMapper: CheckNameDtoMapper,
    private val registrationDtoMapper: RegistrationDtoMapper
): BaseRemoteData(), AccountRemoteData {
    override suspend fun checkUsernameTaken(checkNameRequest: CheckNameRequest): Resource<CheckNameModel> = getResult({
        accountService.checkName(checkNameRequest.userName)
    }, checkNameDtoMapper)

    override suspend fun setupProfile(setupProfileRequest: SetupProfileRequest): Resource<AccountModel> = getResult({
        accountService.setupProfile(
            SetupProfileRequestDto(
                setupProfileRequest
            )
        )
    }, accountDtoMapper)

    override suspend fun attemptAuthentication(): Resource<AccountModel> = getResult({
        accountService.authenticate()
    }, accountDtoMapper)

    override suspend fun attemptAuthentication(
        emailAddress: String,
        password: String
    ): Resource<AccountModel> = getResult({
        // Convert these credentials to basic authorization.
        val credentials = Credentials.basic(emailAddress, password)
        // Now, authenticate with remote.
        accountService.authenticate(credentials)
    }, accountDtoMapper)

    override suspend fun logout(): Resource<AccountModel> = getResult({
        // Simply call the logout service function.
        accountService.logout()
    }, accountDtoMapper)

    override suspend fun registerLocalAccount(registerLocalAccountRequest: RegisterLocalAccountRequest): Resource<RegistrationModel> = getResult({
        accountService.registerLocalAccount(
            RegisterLocalAccountRequestDto(registerLocalAccountRequest)
        )
    }, registrationDtoMapper)
}