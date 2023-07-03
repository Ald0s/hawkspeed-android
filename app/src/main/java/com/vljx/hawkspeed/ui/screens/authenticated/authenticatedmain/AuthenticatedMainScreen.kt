package com.vljx.hawkspeed.ui.screens.authenticated.authenticatedmain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen

@Composable
fun AuthenticatedMainScreen(
    onAuthenticatedAndSetup: () -> Unit,
    onVerificationRequired: (String) -> Unit,
    onSetupRequired: (String) -> Unit,
    onLostAuthentication: () -> Unit,
    authenticatedMainViewModel: AuthenticatedMainViewModel = hiltViewModel()
) {
    val currentOnVerificationRequired by rememberUpdatedState(onVerificationRequired)
    val currentOnSetupRequired by rememberUpdatedState(onSetupRequired)
    val currentOnLostAuthentication by rememberUpdatedState(onLostAuthentication)
    val authenticatedMainUiState: AuthenticatedMainUiState by authenticatedMainViewModel.authenticatedMainUiState.collectAsState()

    when(authenticatedMainUiState) {
        is AuthenticatedMainUiState.Authenticated -> {
            // We are authenticated and there's no further work to do. We'll now navigate toward the world map screen.
            LaunchedEffect(key1 = Unit, block = {
                onAuthenticatedAndSetup()
            })
        }
        is AuthenticatedMainUiState.AuthenticatedAccountNotVerified -> {
            // We must verify this account. Invoke callback in a launched side effect.
            LaunchedEffect(key1 = Unit, block = {
                currentOnVerificationRequired((authenticatedMainUiState as AuthenticatedMainUiState.AuthenticatedAccountNotVerified).userUid)
            })
        }
        is AuthenticatedMainUiState.AuthenticatedSetupRequired -> {
            // We are authenticated, but some sort of setup is required in order to continue.  Invoke callback in a launched side effect.
            LaunchedEffect(key1 = Unit, block = {
                currentOnSetupRequired((authenticatedMainUiState as AuthenticatedMainUiState.AuthenticatedSetupRequired).userUid)
            })
        }
        is AuthenticatedMainUiState.NotAuthenticated -> {
            // We are not authenticated. This screen, and all others dependant on it are not allowed anymore. So we will invoke not authenticated callback,
            // which should pop us all the way back to the onboard nav graph.
            LaunchedEffect(key1 = Unit, block = {
                currentOnLostAuthentication()
            })
        }
        else -> {
            // Nothing to do here, this just catches idle state.
        }
    }
    // Call the loading composable here.
    LoadingScreen()
}