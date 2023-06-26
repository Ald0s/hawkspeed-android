package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.BuildConfig
import com.vljx.hawkspeed.data.models.account.AccountModel
import com.vljx.hawkspeed.data.models.account.CheckNameModel
import com.vljx.hawkspeed.data.models.account.RegistrationModel
import com.vljx.hawkspeed.data.network.api.AccountService
import com.vljx.hawkspeed.data.network.mapper.account.AccountDtoMapper
import com.vljx.hawkspeed.data.network.mapper.account.CheckNameDtoMapper
import com.vljx.hawkspeed.data.network.mapper.account.RegistrationDtoMapper
import com.vljx.hawkspeed.data.network.requestmodels.RequestRegisterLocalAccountDto
import com.vljx.hawkspeed.data.network.requestmodels.RequestSetupProfileDto
import com.vljx.hawkspeed.data.source.account.AccountRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.exc.NoSessionCookieException
import com.vljx.hawkspeed.domain.requestmodels.account.RequestCheckName
import com.vljx.hawkspeed.domain.requestmodels.account.RequestLogin
import com.vljx.hawkspeed.domain.requestmodels.account.RequestRegisterLocalAccount
import com.vljx.hawkspeed.domain.requestmodels.account.RequestSetupProfile
import okhttp3.Credentials
import java.net.CookieManager
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import javax.inject.Inject

class AccountRemoteDataImpl @Inject constructor(
    private val accountService: AccountService,
    private val cookieManager: CookieManager,

    private val accountDtoMapper: AccountDtoMapper,
    private val checkNameDtoMapper: CheckNameDtoMapper,
    private val registrationDtoMapper: RegistrationDtoMapper
): BaseRemoteData(), AccountRemoteData {
    override suspend fun queryCurrentAccount(): Resource<AccountModel> = getResult({
        accountService.authenticate()
    }, accountDtoMapper)

    override suspend fun attemptAuthentication(): Resource<AccountModel> = getResult({
        // Before we authenticate, check that we have at least one item in applicable cookies list.
        if(getApplicableCookies().isEmpty()) {
            // Throw an exception for this, to be handled in base remote data.
            throw NoSessionCookieException()
        }
        accountService.authenticate()
    }, accountDtoMapper)

    override suspend fun attemptAuthentication(requestLogin: RequestLogin): Resource<AccountModel> = getResult({
        // Convert these credentials to basic authorization.
        val credentials = Credentials.basic(requestLogin.emailAddress, requestLogin.password)
        // Now, authenticate with remote.
        accountService.authenticate(credentials)
    }, accountDtoMapper)

    override suspend fun checkUsernameTaken(requestCheckName: RequestCheckName): Resource<CheckNameModel> = getResult({
        accountService.checkName(requestCheckName.userName)
    }, checkNameDtoMapper)

    override suspend fun setupAccountProfile(requestSetupProfile: RequestSetupProfile): Resource<AccountModel> = getResult({
        accountService.setupProfile(
            RequestSetupProfileDto(
                requestSetupProfile
            )
        )
    }, accountDtoMapper)

    override suspend fun registerLocalAccount(requestRegisterLocalAccount: RequestRegisterLocalAccount): Resource<RegistrationModel> = getResult({
        accountService.registerLocalAccount(
            RequestRegisterLocalAccountDto(requestRegisterLocalAccount)
        )
    }, registrationDtoMapper)

    override suspend fun clearCookie() {
        val cookieStore: CookieStore = cookieManager.cookieStore
        val httpUri = URI.create(BuildConfig.SERVICE_URL)
        // Get applicable cookies.
        val applicableCookies: List<HttpCookie> = getApplicableCookies()
        // Now remove all cookies.
        if(applicableCookies.isNotEmpty()) {
            applicableCookies.forEach { httpCookie ->
                cookieStore.remove(httpUri, httpCookie)
            }
        }
    }

    override suspend fun logout(): Resource<AccountModel> = getResult({
        accountService.logout()
    }, accountDtoMapper)

    override fun getApplicableCookies(): List<HttpCookie> {
        val cookieStore: CookieStore = cookieManager.cookieStore
        val httpUri = URI.create(BuildConfig.SERVICE_URL)
        return cookieStore.get(httpUri)
    }
}