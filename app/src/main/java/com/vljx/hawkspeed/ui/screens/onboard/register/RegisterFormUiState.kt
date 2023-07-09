package com.vljx.hawkspeed.ui.screens.onboard.register

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.ui.component.InputValidationResult

sealed class RegisterFormUiState {
    /**
     * A state that contains the state of the registration forms' contents.
     */
    data class RegistrationForm(
        val validateEmailAddress: InputValidationResult,
        val validatePassword: InputValidationResult,
        val validateConfirmPassword: InputValidationResult,
        val canAttemptRegistration: Boolean
    ): RegisterFormUiState()

    /**
     * A state that indicates a registration attempt is in progress.
     */
    object AttemptingRegistration: RegisterFormUiState()

    /**
     * When the registration attempt has failed.
     */
    data class RegistrationFailed(
        val resourceError: ResourceError
    ): RegisterFormUiState()
}