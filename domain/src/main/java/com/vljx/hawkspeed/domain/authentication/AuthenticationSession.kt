package com.vljx.hawkspeed.domain.authentication

import com.vljx.hawkspeed.domain.models.account.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationSession @Inject constructor(

) {
    private val mutableAuthenticationState: MutableStateFlow<AuthenticationState> =
        MutableStateFlow(AuthenticationState.NotAuthenticated)

    val authenticationState: StateFlow<AuthenticationState> =
        mutableAuthenticationState

    val isAuthentication: Boolean
        get() = authenticationState.value is AuthenticationState.Authenticated

    /**
     *
     */
    fun updateCurrentAuthentication(account: Account) {
        mutableAuthenticationState.tryEmit(
            AuthenticationState.Authenticated(account)
        )
    }

    /**
     *
     */
    fun clearAuthentication() {
        mutableAuthenticationState.tryEmit(
            AuthenticationState.NotAuthenticated
        )
    }
}