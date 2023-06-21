package com.vljx.hawkspeed.ui.screens.authenticated.verify

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Account

sealed class VerifyAccountUiState {
    // A loading state.
    object Loading: VerifyAccountUiState()

    // Account is now verified.
    data class AccountVerified(
        val account: Account
    ): VerifyAccountUiState()

    // Account is not yet verified.
    data class AccountNotVerified(
        val account: Account
    ): VerifyAccountUiState()

    // Failed.
    data class Failed(
        val resourceError: ResourceError
    ): VerifyAccountUiState()
}