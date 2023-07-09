package com.vljx.hawkspeed.ui.screens.onboard.login

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.ui.component.InputValidationResult

sealed class LoginFormUiState {
    /**
     * A state that indicates the current contents of the login form.
     */
    data class LoginForm(
        val validateEmailAddress: InputValidationResult,
        val validatePassword: InputValidationResult,
        val canAttemptLogin: Boolean
    ): LoginFormUiState()

    /**
     * A state that indicates a login attempt is in progress.
     */
    object LoggingIn: LoginFormUiState()

    /**
     * A state that indicates a login attempt has failed.
     */
    data class LoginFailed(
        val resourceError: ResourceError
    ): LoginFormUiState()
}