package com.vljx.hawkspeed.ui.screens.splash

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Account

@Composable
fun SplashScreen(
    onAuthenticationSuccessful: (Account) -> Unit,
    onAuthenticationFailed: (ResourceError) -> Unit,
    splashViewModel: SplashViewModel = hiltViewModel()
) {
    // Collect the splash UI state flow as a state here, which means this screen will be recomposed each time that changes.
    val splashUiState: SplashUiState by splashViewModel.splashUiState.collectAsState()

    // Now, open a when conditional here to check which state this is.
    when(splashUiState) {
        is SplashUiState.SuccessfulAuthentication -> {
            // Succeeded in authenticating. Call the success function within a launched side effect, since this change is being invoked from a flow.
            LaunchedEffect(key1 = Unit, block = {
                onAuthenticationSuccessful((splashUiState as SplashUiState.SuccessfulAuthentication).account)
            })
        }
        is SplashUiState.Loading -> {
            // We are now loading. Simply draw a progress indicator below.
        }
        is SplashUiState.Failed -> {
            // We failed authentication. Call the fail function within a launched side effect, since this change is being invoked from a flow.
            LaunchedEffect(key1 = Unit, block = {
                onAuthenticationFailed((splashUiState as SplashUiState.Failed).resourceError)
            })
        }
    }
    // We'll draw our circular progress indicator here.
    // TODO: improve the UI here.
    CircularProgressIndicator()
}