package com.vljx.hawkspeed.ui.screens.onboard.login

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Account

sealed class LoginUiState {
    // The initial state for the login UI.
    object Idle: LoginUiState()

    // The success condition, when a login has been achieved.
    data class SuccessfulLogin(
        val account: Account
    ): LoginUiState()

    // The loading condition, when a login is being attempted.
    object LoggingIn: LoginUiState()

    // The fail condition, when a login has failed.
    data class Failed(
        val resourceError: ResourceError
    ): LoginUiState()
}