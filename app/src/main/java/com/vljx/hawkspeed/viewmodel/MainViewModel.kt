package com.vljx.hawkspeed.viewmodel

import androidx.lifecycle.ViewModel
import com.vljx.hawkspeed.domain.authentication.AuthenticationSession
import com.vljx.hawkspeed.domain.authentication.AuthenticationState
import com.vljx.hawkspeed.domain.models.account.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authenticationSession: AuthenticationSession
): ViewModel() {
    /**
     * A flow for the current account we are logged into.
     * For now, this can't be null, and will map the latest authenticated state flow if we are authenticated or throw an exception.
     * TODO: However, if this causes trouble, we can change this to a nullable account and transform the latest instead.
     */
    val currentAccount: Flow<Account> =
        authenticationSession.authenticationState.map { value: AuthenticationState ->
            if(value is AuthenticationState.Authenticated) {
                // Map to the account in question.
                Account(
                    value.userUid,
                    value.emailAddress,
                    value.userName,
                    value.isVerified,
                    value.isPasswordVerified,
                    value.isProfileSetup
                )
            } else {
                // TODO: properly handle this? it's unlikely this could ever be triggered under normal circumstances, but there's a chance if the user ever decides to log out.
                throw NotImplementedError("MainViewModel::currentAccount flow, NotAuthenticated state is unhandled!")
            }
        }


}