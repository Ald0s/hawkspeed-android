package com.vljx.hawkspeed.ui.screens.authenticated.authenticatedmain

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.authentication.AuthenticationSession
import com.vljx.hawkspeed.domain.authentication.AuthenticationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AuthenticatedMainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authenticationSession: AuthenticationSession
): ViewModel() {
    /**
     * Get the User UID for the User that has just been authenticated.
     */
    private val userUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_USER_UID]))

    /**
     * Map the authentication state from current session to a main UI state. This should recompose the entire app, no matter where the User is
     * if authentication happens to change to not authenticated.
     */
    val authenticatedMainUiState: StateFlow<AuthenticatedMainUiState> =
        authenticationSession.authenticationState.map { value: AuthenticationState ->
            when(value) {
                is AuthenticationState.Authenticated -> {
                    if(!value.anySetupOrCompletionRequired) {
                        // No setup required, just send out the authenticated state.
                        AuthenticatedMainUiState.Authenticated(value.userUid, value.userName!!, value.emailAddress)
                    } else if(!value.isVerified) {
                        // User has not verified their account just yet.
                        AuthenticatedMainUiState.AuthenticatedAccountNotVerified(value.userUid, value.emailAddress)
                    } else if(!value.isPasswordVerified) {
                        // TODO: password not being verified.
                        throw NotImplementedError("isPasswordVerified being false is not yet handled in AuthenticatedMainViewModel.")
                    } else if(!value.isProfileSetup) {
                        // User is authenticated but their profile is not yet verified.
                        AuthenticatedMainUiState.AuthenticatedSetupRequired(value.userUid, value.emailAddress)
                    } else {
                        throw NotImplementedError("User $value is authenticated, but not setup in a way that is not yet handled.")
                    }
                }
                is AuthenticationState.NotAuthenticated -> AuthenticatedMainUiState.NotAuthenticated
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AuthenticatedMainUiState.Idle)

    companion object {
        const val ARG_USER_UID = "userUid"
    }
}