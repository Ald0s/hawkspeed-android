package com.vljx.hawkspeed.ui.screens.splash

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Account

sealed class SplashUiState {
    data class SuccessfulAuthentication(
        val account: Account
    ): SplashUiState()

    object Loading: SplashUiState()

    data class Failed(
        val resourceError: ResourceError
    ): SplashUiState()
}