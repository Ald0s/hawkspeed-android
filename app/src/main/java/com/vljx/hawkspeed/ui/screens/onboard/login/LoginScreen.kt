package com.vljx.hawkspeed.ui.screens.onboard.login

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.domain.models.account.Account
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccessful: (Account) -> Unit,
    onRegisterLocalAccountClicked: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    // Collect the login ui state here, idle state as initial.
    val loginUiState: LoginUiState by loginViewModel.loginUiState.collectAsState(initial = LoginUiState.Idle)

    when(loginUiState) {
        is LoginUiState.SuccessfulLogin -> {
            // Login was successful, invoke success callback on launched side effect.
            LaunchedEffect(key1 = Unit, block = {
                onLoginSuccessful((loginUiState as LoginUiState.SuccessfulLogin).account)
            })
        }
        is LoginUiState.LoggingIn -> {
            // Busy logging in, this is where we'll replace the button with a circular progress indicator.
            // TODO: replace button.
        }
        is LoginUiState.Failed -> {
            // Failed. We must handle the error locally here.
            Timber.e("Failed to login! Got error:\n${(loginUiState as LoginUiState.Failed).resourceError}")
        }
        is LoginUiState.Idle -> {
            // Idle. Don't do anything.
        }
    }

    val emailAddress: String? by loginViewModel.emailAddress.collectAsState()
    val password: String? by loginViewModel.password.collectAsState()

    // Now, load the form itself.
    // TODO: improve the UI here.
    // TODO: A button here for navigating to the register screen for a new local account.
    Column {
        TextField(
            value = emailAddress ?: "",
            onValueChange = loginViewModel::updateEmailAddress
        )
        TextField(
            value = password ?: "",
            onValueChange = loginViewModel::updatePassword
        )
        Button(
            onClick = loginViewModel::attemptLogin
        ) {
            Text(text = "Login")
        }
    }
}