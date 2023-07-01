package com.vljx.hawkspeed.ui.screens.splash

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme

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
    SplashForm(
        splashUiState = splashUiState
    )
}

@Composable
fun SplashForm(
    splashUiState: SplashUiState,
    @StringRes loadingResId: Int = R.string.loading
) {
    LoadingScreen(
        loadingResId = loadingResId
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSplashForm(

) {
    HawkSpeedTheme {
        SplashForm(splashUiState = SplashUiState.Loading)
    }
}