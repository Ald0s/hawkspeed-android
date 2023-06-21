package com.vljx.hawkspeed.ui.screens.onboard.register

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.domain.models.account.Registration

@Composable
fun RegisterScreen(
    onRegistered: (Registration) -> Unit,
    registerViewModel: RegisterViewModel = hiltViewModel()
) {

}