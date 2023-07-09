package com.vljx.hawkspeed.ui.screens.onboard.login

import com.vljx.hawkspeed.domain.models.account.Account

sealed class LoginUiState {
    /**
     * A success state that indicates the User successfully logged in.
     */
    data class SuccessfulLogin(
        val account: Account
    ): LoginUiState()

    /**
     * A state that indicates the login form should be shown.
     */
    data class ShowLoginForm(
        val loginFormUiState: LoginFormUiState
    ): LoginUiState()

    /**
     * The default state.
     */
    object Loading: LoginUiState()
}