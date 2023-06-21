package com.vljx.hawkspeed.ui.screens.authenticated.setup

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Account

sealed class SetupAccountUiState {
    // The idle state for this UI.
    object Idle: SetupAccountUiState()

    // The loading state.
    object Loading: SetupAccountUiState()

    // Successful state for setting this UI up.
    data class AccountSetup(
        val account: Account
    ): SetupAccountUiState()

    // Failed state for setting this UI up.
    data class Failed(
        val resourceError: ResourceError
    ): SetupAccountUiState()
}