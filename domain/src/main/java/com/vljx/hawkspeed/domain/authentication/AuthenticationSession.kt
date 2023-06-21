package com.vljx.hawkspeed.domain.authentication

import com.vljx.hawkspeed.domain.base.ApiErrorWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationSession @Inject constructor(

) {
    private val mutableCurrentAuthentication = MutableStateFlow<AuthenticationState>(AuthenticationState.NotAuthenticated)
    val authenticationState: StateFlow<AuthenticationState> =
        mutableCurrentAuthentication

    val isAuthenticated: Boolean
        get() = authenticationState.value is AuthenticationState.Authenticated

    fun updateCurrentAccount(
        userUid: String,
        emailAddress: String,
        userName: String?,
        isVerified: Boolean,
        isPasswordVerified: Boolean,
        isProfileSetup: Boolean,
        canCreateTracks: Boolean
    ) {
        // Convert to authentication state and try emit to mutable current authentication.
        val authenticationState: AuthenticationState = AuthenticationState.Authenticated(
            userUid,
            emailAddress,
            userName,
            isVerified,
            isPasswordVerified,
            isProfileSetup,
            canCreateTracks
        )
        mutableCurrentAuthentication.tryEmit(authenticationState)
    }

    /**
     * Clear the current account, providing the account that was in use, but no error - meaning this was an intended logout request.
     */
    fun clearCurrentAccount() {
        // Simply try emit an Unauthenticated state.
        mutableCurrentAuthentication.tryEmit(AuthenticationState.NotAuthenticated)
    }

    /**
     * Clear the current account, given an api error but not a current account. This means an error caused the logout.
     */
    fun clearCurrentAccount(apiErrorWrapper: ApiErrorWrapper) {
        // Simply try emit an Unauthenticated state.
        mutableCurrentAuthentication.tryEmit(AuthenticationState.NotAuthenticated)
    }
}