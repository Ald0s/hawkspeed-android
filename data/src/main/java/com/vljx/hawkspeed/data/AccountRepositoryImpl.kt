package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.account.AccountMapper
import com.vljx.hawkspeed.data.mapper.account.CheckNameMapper
import com.vljx.hawkspeed.data.mapper.account.RegistrationMapper
import com.vljx.hawkspeed.data.models.account.AccountModel
import com.vljx.hawkspeed.data.source.AccountLocalData
import com.vljx.hawkspeed.data.source.AccountRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.exc.AccountChangedException
import com.vljx.hawkspeed.domain.exc.AccountChangedException.Companion.ERROR_ACCOUNT_CHANGED
import com.vljx.hawkspeed.domain.exc.ResourceErrorException
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.account.CheckName
import com.vljx.hawkspeed.domain.models.account.Registration
import com.vljx.hawkspeed.domain.repository.AccountRepository
import com.vljx.hawkspeed.domain.requestmodels.account.RequestCheckName
import com.vljx.hawkspeed.domain.requestmodels.account.RequestLogin
import com.vljx.hawkspeed.domain.requestmodels.account.RequestRegisterLocalAccount
import com.vljx.hawkspeed.domain.requestmodels.account.RequestSetupProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountLocalData: AccountLocalData,
    private val accountRemoteData: AccountRemoteData,

    private val accountMapper: AccountMapper,
    private val registrationMapper: RegistrationMapper,
    private val checkNameMapper: CheckNameMapper
): BaseRepository(), AccountRepository {
    override suspend fun getCurrentAccount(): Resource<Account> =
        queryNetworkAndCache(
            accountMapper,
            networkQuery = { accountRemoteData.queryCurrentAccount() },
            cacheResult = { accountModel ->
                // Upsert latest account into cache, and also update authentication session.
                updateLocalAccount(accountModel)
            }
        )

    override fun getCurrentCachedAccount(): Flow<Account?> =
        flowFromCache(
            accountMapper,
            databaseQuery = { accountLocalData.selectCurrentAccount() }
        )

    override fun attemptAuthentication(): Flow<Resource<Account>> =
        flowQueryNetworkAndCache(
            accountMapper,
            networkQuery = { accountRemoteData.attemptAuthentication() },
            cacheResult = { accountModel ->
                // Upsert latest account into cache, and also update authentication session.
                updateLocalAccount(accountModel)
            }
        )

    override fun attemptLocalAuthentication(requestLogin: RequestLogin): Flow<Resource<Account>> =
        flowQueryNetworkAndCache(
            accountMapper,
            networkQuery = { accountRemoteData.attemptAuthentication(requestLogin) },
            cacheResult = { accountModel ->
                // Upsert latest account into cache, and also update authentication session.
                updateLocalAccount(accountModel)
            }
        )

    override suspend fun checkUsernameTaken(requestCheckName: RequestCheckName): Resource<CheckName> =
        queryNetworkNoCache(
            checkNameMapper,
            networkQuery = { accountRemoteData.checkUsernameTaken(requestCheckName) }
        )

    override fun setupAccountProfile(requestSetupProfile: RequestSetupProfile): Flow<Resource<Account>> =
        flowQueryNetworkAndCache(
            accountMapper,
            networkQuery = { accountRemoteData.setupAccountProfile(requestSetupProfile) },
            cacheResult = { accountModel ->
                // Upsert latest account into cache, and also update authentication session.
                updateLocalAccount(accountModel)
            }
        )

    override fun registerLocalAccount(params: RequestRegisterLocalAccount): Flow<Resource<Registration>> =
        flowQueryNetworkNoCache(
            registrationMapper,
            networkQuery = { accountRemoteData.registerLocalAccount(params) }
        )

    override suspend fun logout(): Resource<Account> =
        queryNetworkNoCache(
            accountMapper,
            networkQuery = {
                try {
                    // Irrespective of outcome, we'll clear the account in cache and also the account in session.
                    accountLocalData.clearAccount()
                    authenticationSession.clearCurrentAccount()
                    // Query the logout route to inform server. This can fail.
                    accountRemoteData.logout()
                } finally {
                    // Clear cookie for hawkspeed no matter what.
                    accountRemoteData.clearCookie()
                }
            }
        )

    private suspend fun updateLocalAccount(accountModel: AccountModel) {
        try {
            // Upsert the Account into cache.
            accountLocalData.upsertAccount(accountModel)
            // Set the current account on our session component.
            authenticationSession.updateCurrentAccount(
                accountModel.userUid,
                accountModel.emailAddress,
                accountModel.userName,
                accountModel.isAccountVerified,
                accountModel.isPasswordVerified,
                accountModel.isProfileSetup,
                accountModel.canCreateTracks
            )
        } catch(ace: AccountChangedException) {
            // If we get an account changed exception, we'll clear our cache, we'll also clear account from authentication session, the contents
            // of data store and finally we'll clear all hawkspeed cookies.
            accountLocalData.clearAccount()
            accountRemoteData.clearCookie()
            authenticationSession.clearCurrentAccount()
            // We'll build a resource error with the account changed exception and throw that so base repository returns that as a Resource error.
            throw ResourceErrorException(
                ResourceError.GeneralError(ERROR_ACCOUNT_CHANGED, ace)
            )
        }
    }
}