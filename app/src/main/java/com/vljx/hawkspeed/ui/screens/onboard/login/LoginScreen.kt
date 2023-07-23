package com.vljx.hawkspeed.ui.screens.onboard.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.ui.component.InputValidationResult
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import timber.log.Timber

@Composable
fun LoginScreen(
    onLoginSuccessful: (Account) -> Unit,
    onRegisterLocalAccountClicked: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val loginUiState: LoginUiState by loginViewModel.loginUiState.collectAsState()
    when(loginUiState) {
        is LoginUiState.SuccessfulLogin -> {
            // Login was successful, invoke success callback on launched side effect.
            LaunchedEffect(key1 = Unit, block = {
                onLoginSuccessful((loginUiState as LoginUiState.SuccessfulLogin).account)
            })
            // Show loading screen on success.
            LoadingScreen()
        }

        is LoginUiState.ShowLoginForm -> {
            val emailAddressState: String? by loginViewModel.emailAddressState.collectAsState()
            val passwordState: String? by loginViewModel.passwordState.collectAsState()

            LoginForm(
                showLoginForm = loginUiState as LoginUiState.ShowLoginForm,
                emailAddress = emailAddressState,
                password = passwordState,

                updateEmailAddress = loginViewModel::updateEmailAddress,
                updatePassword = loginViewModel::updatePassword,
                onRegisterClicked = onRegisterLocalAccountClicked,
                onAttemptLoginClicked = loginViewModel::attemptLogin
            )
        }

        is LoginUiState.Loading ->
            LoadingScreen()
    }
}

/**
 * TODO: validation must still be applied to all form controls.
 */
@Composable
fun LoginForm(
    showLoginForm: LoginUiState.ShowLoginForm,
    emailAddress: String?,
    password: String?,

    updateEmailAddress: ((String) -> Unit)? = null,
    updatePassword: ((String) -> Unit)? = null,
    onRegisterClicked: (() -> Unit)? = null,
    onAttemptLoginClicked: (() -> Unit)? = null
) {
    var canAttemptLogin: Boolean by remember { mutableStateOf(false) }
    var isLoggingIn: Boolean by remember { mutableStateOf(false) }
    var loginError: ResourceError? by remember { mutableStateOf(null) }

    when(val loginFormUiState = showLoginForm.loginFormUiState) {
        is LoginFormUiState.LoginForm -> {
            canAttemptLogin = loginFormUiState.canAttemptLogin
            isLoggingIn = false
        }
        is LoginFormUiState.LoggingIn -> {
            isLoggingIn = true
            loginError = null
        }
        is LoginFormUiState.LoginFailed -> {
            isLoggingIn = false
            loginError = loginFormUiState.resourceError
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxHeight()
    ) { paddingValues ->
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.8f)
            ) {
                Spacer(modifier = Modifier.height(128.dp))

                Text(
                    letterSpacing = 3.6.sp,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displayMedium,
                    text = stringResource(id = R.string.app_name).uppercase()
                )

                Spacer(modifier = Modifier.height(56.dp))

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "email"
                        )
                    },
                    value = emailAddress ?: "",
                    placeholder = {
                        Text(text = stringResource(id = R.string.placeholder_email))
                    },
                    onValueChange = updateEmailAddress ?: {}
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "password"
                        )
                    },
                    value = password ?: "",
                    placeholder = {
                        Text(text = stringResource(id = R.string.placeholder_password))
                    },
                    onValueChange = updatePassword ?: {}
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    enabled = !isLoggingIn && canAttemptLogin,
                    onClick = onAttemptLoginClicked ?: {},
                    shape = RectangleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.login_attempt_login).uppercase()
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = onRegisterClicked ?: {}
                ) {
                    Text(
                        text = stringResource(id = R.string.login_sign_up).uppercase()
                    )
                }
            }
        }

    }
}

@Preview
@Composable
fun PreviewLoginForm(

) {
    HawkSpeedTheme {
        LoginForm(
            showLoginForm = LoginUiState.ShowLoginForm(
                loginFormUiState = LoginFormUiState.LoginForm(
                    InputValidationResult(true),
                    InputValidationResult(false),
                    false
                )
            ),
            emailAddress = "user1@mail.com",
            password = null
        )
    }
}