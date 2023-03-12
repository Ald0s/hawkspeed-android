package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.account.AccountMapper
import com.vljx.hawkspeed.data.mapper.account.CheckNameMapper
import com.vljx.hawkspeed.data.mapper.account.RegistrationMapper
import com.vljx.hawkspeed.data.source.AccountLocalData
import com.vljx.hawkspeed.data.source.AccountRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.authentication.AuthenticationSession
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.account.CheckName
import com.vljx.hawkspeed.domain.models.account.Registration
import com.vljx.hawkspeed.domain.repository.AccountRepository
import com.vljx.hawkspeed.domain.requests.CheckNameRequest
import com.vljx.hawkspeed.domain.requests.LoginRequest
import com.vljx.hawkspeed.domain.requests.RegisterLocalAccountRequest
import com.vljx.hawkspeed.domain.requests.SetupProfileRequest
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.net.CookieManager
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val authenticationSession: AuthenticationSession,
    private val cookieManager: CookieManager,

    private val accountLocalData: AccountLocalData,
    private val accountRemoteData: AccountRemoteData,

    private val accountMapper: AccountMapper,
    private val registrationMapper: RegistrationMapper,
    private val checkNameMapper: CheckNameMapper
): BaseRepository(), AccountRepository {
    override fun getAccountByUid(userUid: String): Flow<Resource<Account>> =
        fromCache(
            accountMapper,
            databaseQuery = { accountLocalData.selectAccountByUid(userUid) }
        )

    override suspend fun checkUsernameTaken(checkNameRequest: CheckNameRequest): Flow<Resource<CheckName>> =
        queryNoCache(
            checkNameMapper,
            networkQuery = { accountRemoteData.checkUsernameTaken(checkNameRequest) }
        )

    override suspend fun setupProfile(setupProfileRequest: SetupProfileRequest): Flow<Resource<Account>> =
        queryAndCache(
            accountMapper,
            networkQuery = { accountRemoteData.setupProfile(setupProfileRequest) },
            cacheResult = { accountLocalData.upsertAccount(it) }
        )

    override suspend fun attemptAuthentication(): Flow<Resource<Account>> =
        queryAndCache(
            accountMapper,
            networkQuery = {
                // Perform the network query without any parameters.
                accountRemoteData.attemptAuthentication()
            },
            cacheResult = { accountModel ->
                // Now cache the account.
                // TODO: here's where we'll also deal with the cookie set by the server for us.
                accountLocalData.setAccountLoggedIn(accountModel)
                // Set the current account on our session component.
                authenticationSession.updateCurrentAuthentication(
                    accountMapper.mapFromData(accountModel)
                )
            }
        )

    override suspend fun attemptAuthentication(loginRequest: LoginRequest): Flow<Resource<Account>> =
        queryAndCache(
            accountMapper,
            networkQuery = {
                // Perform the network query with the email address & password.
                accountRemoteData.attemptAuthentication(loginRequest.emailAddress, loginRequest.password)
            },
            cacheResult = { accountModel ->
                // Now cache the account.
                // TODO: here's where we'll also deal with the cookie set by the server for us.
                accountLocalData.setAccountLoggedIn(accountModel)
                // Set the current account on our session component.
                authenticationSession.updateCurrentAuthentication(
                    accountMapper.mapFromData(accountModel)
                )
            }
        )

    override suspend fun logout(): Flow<Resource<Account>> =
        queryAndCache(
            accountMapper,
            networkQuery = { accountRemoteData.logout() },
            cacheResult = { accountModel ->
                // Cache the resulting account model, however, we will do so slightly differently; we'll just report this account is
                // no longer in active use. Or should we delete it from the database... ?
                accountLocalData.accountLoggedOut(accountModel)
                // Clear any currently logged in account.
                authenticationSession.clearAuthentication()

                // As well as that, we will also clear the cookie associated with the current service URL right now.
                // TODO: is this correct placement for this... ? Or should this be in the remote data impl... ?
                val cookieStore: CookieStore = cookieManager.cookieStore
                // Remove based on a URI constructed from BuildConfig.
                val httpUri = URI.create(BuildConfig.SERVICE_URL)
                // Get all cookies.
                val applicableCookies: List<HttpCookie> = cookieStore.get(httpUri)
                if(applicableCookies.isNotEmpty()) {
                    // Remove all cookies.
                    applicableCookies.forEach { httpCookie ->
                        cookieStore.remove(httpUri, httpCookie)
                    }
                    Timber.d("While logging out, successfully removed ${applicableCookies.size} cookies associated with $httpUri")
                } else {
                    Timber.w("During logout, no cookies applicable to $httpUri could be located!")
                }
            }
        )

    override suspend fun registerLocalAccount(params: RegisterLocalAccountRequest): Flow<Resource<Registration>> =
        queryNoCache(
            registrationMapper,
            networkQuery = { accountRemoteData.registerLocalAccount(params) }
        )
}