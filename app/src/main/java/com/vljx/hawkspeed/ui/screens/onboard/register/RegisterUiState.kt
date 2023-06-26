package com.vljx.hawkspeed.ui.screens.onboard.register

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Registration

sealed class RegisterUiState {
    /**
     * The default state.
     */
    object Idle: RegisterUiState()

    /**
     * When a registration attempt is taking place.
     */
    object Loading: RegisterUiState()

    /**
     * When the registration attempt has succeeded.
     */
    data class RegistrationSuccessful(
        val registration: Registration
    ): RegisterUiState()

    /**
     * When the registration attempt has failed.
     */
    data class RegistrationFailed(
        val resourceError: ResourceError
    ): RegisterUiState()
}