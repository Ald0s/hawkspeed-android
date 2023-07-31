package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.account.CheckName
import com.vljx.hawkspeed.domain.models.account.Registration
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.requestmodels.account.RequestCheckName
import com.vljx.hawkspeed.domain.requestmodels.account.RequestLogin
import com.vljx.hawkspeed.domain.requestmodels.account.RequestRegisterLocalAccount
import com.vljx.hawkspeed.domain.requestmodels.account.RequestSetupProfile
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    /**
     * Open a flow for the current account in use, but a network query will also be performed to update that account in cache.
     */
    suspend fun getCurrentAccount(): Resource<Account>

    /**
     * Open a flow for the current account in use. This does not perform any network queries and will instead just watch a query for the
     * account instance currently entered in cache.
     */
    fun getCurrentCachedAccount(): Flow<Account?>

    /**
     * Open a flow for the current User's settings. This will construct a settings state from relevant data in both cache and preferences.
     */
    fun getCurrentSettings(): Flow<GameSettings?>

    /**
     * Attempt a (re)authentication of the current User's account. This will first use the cookie currently stored for HawkSpeed, and only
     * upon failure will that cookie be cleared and a full authentication be required. This function will therefore attempt to authenticate,
     * then on success will upsert the latest User's account.
     */
    fun attemptAuthentication(): Flow<Resource<Account>>

    /**
     * Attempt an authentication for a locally registered account. This will send an authorisation request and on success will store the
     * latest account for the User.
     */
    fun attemptLocalAuthentication(requestLogin: RequestLogin): Flow<Resource<Account>>

    /**
     * Perform a query to determine whether the username given by the request model is already taken by another User.
     */
    suspend fun checkUsernameTaken(requestCheckName: RequestCheckName): Resource<CheckName>

    /**
     * Perform the setup account profile query.
     */
    fun setupAccountProfile(requestSetupProfile: RequestSetupProfile): Flow<Resource<Account>>

    /**
     * Register a new local account, that is, one with a password.
     */
    fun registerLocalAccount(params: RequestRegisterLocalAccount): Flow<Resource<Registration>>

    /**
     * Log the current User out. This will clear the current account in use whether it succeeds or fails. This function will return an account
     * instance in response, but will not cache it. This is therefore a once-off function. This function will also clear the data store.
     */
    suspend fun logout(): Resource<Account>
}