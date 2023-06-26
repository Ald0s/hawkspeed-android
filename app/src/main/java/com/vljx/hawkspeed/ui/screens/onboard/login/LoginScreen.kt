package com.vljx.hawkspeed.ui.screens.onboard.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
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
import com.vljx.hawkspeed.ui.component.InputValidationResult
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import timber.log.Timber

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
    val validateEmailAddressResult by loginViewModel.validateEmailAddressResult.collectAsState()
    val password: String? by loginViewModel.password.collectAsState()
    val validatePasswordResult by loginViewModel.validatePasswordResult.collectAsState()

    LoginFormUi(
        onRegisterClicked = onRegisterLocalAccountClicked,
        onAttemptLoginClicked = loginViewModel::attemptLogin,
        emailAddress = emailAddress,
        validateEmailAddressResult = validateEmailAddressResult,
        updateEmailAddress = loginViewModel::updateEmailAddress,
        password = password,
        validatePasswordResult = validatePasswordResult,
        updatePassword = loginViewModel::updatePassword
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginFormUi(
    onRegisterClicked: () -> Unit,
    onAttemptLoginClicked: () -> Unit,

    emailAddress: String?,
    validateEmailAddressResult: InputValidationResult,
    updateEmailAddress: (String) -> Unit,

    password: String?,
    validatePasswordResult: InputValidationResult,
    updatePassword: (String) -> Unit,

    resourceError: ResourceError? = null
) {
    Scaffold { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = emailAddress ?: "",
                onValueChange = updateEmailAddress
            )
            OutlinedTextField(
                value = password ?: "",
                onValueChange = updatePassword
            )
            Button(
                onClick = onAttemptLoginClicked
            ) {
                Text(text = stringResource(id = R.string.login_attempt_login))
            }
            TextButton(
                onClick = onRegisterClicked
            ) {
                Text(text = stringResource(id = R.string.login_sign_up))
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun PreviewLoginFormUi(

) {
    HawkSpeedTheme {
        LoginFormUi(
            onRegisterClicked = { /*TODO*/ },
            onAttemptLoginClicked = { /*TODO*/ },
            emailAddress = "user1@mail.com",
            validateEmailAddressResult = InputValidationResult(true),
            updateEmailAddress = { e -> },
            password = "password",
            validatePasswordResult = InputValidationResult(true),
            updatePassword = { e -> }
        )
    }
}