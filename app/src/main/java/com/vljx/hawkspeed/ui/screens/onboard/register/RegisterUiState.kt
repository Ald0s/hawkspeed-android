package com.vljx.hawkspeed.ui.screens.onboard.register

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Registration

sealed class RegisterUiState {
    /**
     * When the registration attempt has succeeded.
     */
    data class RegistrationSuccessful(
        val registration: Registration
    ): RegisterUiState()

    /**
     * A state that indicates the registration form should be shown.
     */
    data class ShowRegistrationForm(
        val registerFormUiState: RegisterFormUiState
    ): RegisterUiState()

    /**
     * When a registration attempt is taking place.
     */
    object Loading: RegisterUiState()
}