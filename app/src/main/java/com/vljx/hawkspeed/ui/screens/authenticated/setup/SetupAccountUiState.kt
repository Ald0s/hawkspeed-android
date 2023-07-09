package com.vljx.hawkspeed.ui.screens.authenticated.setup

import com.vljx.hawkspeed.domain.models.account.Account

sealed class SetupAccountUiState {
    /**
     * The success state, indicating the User has setup their profile.
     */
    data class AccountSetup(
        val account: Account
    ): SetupAccountUiState()

    /**
     * A state that indicates the setup account form should be shown.
     */
    data class ShowSetupAccountForm(
        val setupAccountFormUiState: SetupAccountFormUiState
    ): SetupAccountUiState()

    /**
     * The initial state.
     */
    object Loading: SetupAccountUiState()

}