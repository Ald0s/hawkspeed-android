package com.vljx.hawkspeed.ui.screens.authenticated.authenticatedmain

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapScreen

@Composable
fun AuthenticatedMainScreen(
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
            // We are authenticated and there's no further work to do. We'll now setup the main screen properly.
            AuthenticatedAndSetup(
                authenticatedMainViewModel = authenticatedMainViewModel
            )
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

    // TODO: improve the UI here.
}

@Composable
fun AuthenticatedAndSetup(
    authenticatedMainViewModel: AuthenticatedMainViewModel
) {
    // Get the context we're attached to, and ensure it implements main app service.
    /*val activityContext = LocalContext.current.getActivity()
    if (activityContext !is MainAppService) {
        throw IllegalStateException("Failed to get main activity as MainAppService in MainScreen.")
    }

    LaunchedEffect(key1 = Unit, block = {
        // Send a start command to the app service.
        activityContext.startAppService()
    })*/

    // This is where we'll load the world map.
    WorldMapScreen()
}