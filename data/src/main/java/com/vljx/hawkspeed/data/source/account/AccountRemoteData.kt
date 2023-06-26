package com.vljx.hawkspeed.data.source.account

import com.vljx.hawkspeed.data.models.account.AccountModel
import com.vljx.hawkspeed.data.models.account.CheckNameModel
import com.vljx.hawkspeed.data.models.account.RegistrationModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.account.RequestCheckName
import com.vljx.hawkspeed.domain.requestmodels.account.RequestLogin
import com.vljx.hawkspeed.domain.requestmodels.account.RequestRegisterLocalAccount
import com.vljx.hawkspeed.domain.requestmodels.account.RequestSetupProfile
import java.net.HttpCookie

interface AccountRemoteData {
    /**
     * Query the current User's account. Right now, this will simply query the authentication API route to be served the current User's account.
     */
    suspend fun queryCurrentAccount(): Resource<AccountModel>

    /**
     * Attempt authentication for the current User's account. Without arguments, a HawkSpeed token will be checked for and used in
     * a (re)authentication request.
     */
    suspend fun attemptAuthentication(): Resource<AccountModel>

    /**
     * Attempt authentication for the current User's account given a login local request.
     */
    suspend fun attemptAuthentication(requestLogin: RequestLogin): Resource<AccountModel>

    /**
     * Perform a request to set the current User's profile up, this includes their username vehicle etc.
     */
    suspend fun registerLocalAccount(requestRegisterLocalAccount: RequestRegisterLocalAccount): Resource<RegistrationModel>

    /**
     * Perform a once-off request to check if a username has already been taken by another User.
     */
    suspend fun checkUsernameTaken(requestCheckName: RequestCheckName): Resource<CheckNameModel>

    /**
     * Perform the setup account profile request.
     */
    suspend fun setupAccountProfile(requestSetupProfile: RequestSetupProfile): Resource<AccountModel>

    /**
     * Clear the current HawkSpeed cookie from cache.
     */
    suspend fun clearCookie()

    /**
     * Logout the current User.
     */
    suspend fun logout(): Resource<AccountModel>

    /**
     * Return a list of all cookies that are associated with the HawkSpeed service URL, and are not expired.
     */
    fun getApplicableCookies(): List<HttpCookie>
}