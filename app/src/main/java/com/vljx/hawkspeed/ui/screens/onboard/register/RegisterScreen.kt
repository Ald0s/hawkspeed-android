package com.vljx.hawkspeed.ui.screens.onboard.register

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Registration
import com.vljx.hawkspeed.ui.component.InputValidationResult
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistered: (Registration) -> Unit,
    registerViewModel: RegisterViewModel = hiltViewModel()
) {
    val registerUiState by registerViewModel.registerUiState.collectAsState()
    when(registerUiState) {
        is RegisterUiState.RegistrationSuccessful -> {
            // When registration succeeds, we'll invoke our callback from a launched side effect.
            LaunchedEffect(key1 = Unit, block = {
                onRegistered((registerUiState as RegisterUiState.RegistrationSuccessful).registration)
            })
        }
        is RegisterUiState.ShowRegistrationForm -> {
            val emailAddress: String? by registerViewModel.emailAddress.collectAsState()
            val password: String? by registerViewModel.password.collectAsState()
            val confirmPassword: String? by registerViewModel.confirmPassword.collectAsState()

            RegisterFormUi(
                registerFormUiState = (registerUiState as RegisterUiState.ShowRegistrationForm).registerFormUiState,
                emailAddress = emailAddress,
                password = password,
                confirmPassword = confirmPassword
            )
        }
        is RegisterUiState.Loading ->
            LoadingScreen()
    }
}

/**
 * TODO: validation must still be applied to all form controls.
 */
@Composable
fun RegisterFormUi(
    registerFormUiState: RegisterFormUiState,
    emailAddress: String?,
    password: String?,
    confirmPassword: String?,

    updateEmailAddress: ((String) -> Unit)? = null,
    updatePassword: ((String) -> Unit)? = null,
    updateConfirmPassword: ((String) -> Unit)? = null,
    onAttemptRegistrationClicked: (() -> Unit)? = null
) {
    var validateEmailAddress: InputValidationResult by remember { mutableStateOf(InputValidationResult(false)) }
    var validatePassword: InputValidationResult by remember { mutableStateOf(InputValidationResult(false)) }
    var validateConfirmPassword: InputValidationResult by remember { mutableStateOf(InputValidationResult(false)) }
    var isAttemptingRegistration: Boolean by remember { mutableStateOf(false) }
    var canAttemptRegistration: Boolean by remember { mutableStateOf(false) }
    var registrationError: ResourceError? by remember { mutableStateOf(null) }

    isAttemptingRegistration = registerFormUiState is RegisterFormUiState.AttemptingRegistration
    canAttemptRegistration = registerFormUiState is RegisterFormUiState.RegistrationForm && registerFormUiState.canAttemptRegistration
    when(registerFormUiState) {
        is RegisterFormUiState.RegistrationForm ->  {
            validateEmailAddress = registerFormUiState.validateEmailAddress
            validatePassword = registerFormUiState.validatePassword
            validateConfirmPassword = registerFormUiState.validateConfirmPassword
        }
        is RegisterFormUiState.AttemptingRegistration -> {
            registrationError = null
        }
        is RegisterFormUiState.RegistrationFailed -> {
            registrationError = registerFormUiState.resourceError
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
                Spacer(modifier = Modifier.height(56.dp))
                OutlinedTextField(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "email address"
                        )
                    },
                    value = emailAddress ?: "",
                    placeholder = {
                        Text(text = stringResource(id = R.string.placeholder_email))
                    },
                    onValueChange = updateEmailAddress ?: {},
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
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
                    onValueChange = updatePassword ?: {},
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "confirm password"
                        )
                    },
                    value = confirmPassword ?: "",
                    placeholder = {
                        Text(text = stringResource(id = R.string.placeholder_confirm_password))
                    },
                    onValueChange = updateConfirmPassword ?: {},
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onAttemptRegistrationClicked ?: {},
                    enabled = canAttemptRegistration && !isAttemptingRegistration,
                    shape = RectangleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.register_register).uppercase())
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewRegisterFormUi(

) {
    HawkSpeedTheme {
        RegisterFormUi(
            registerFormUiState = RegisterFormUiState.RegistrationForm(
                InputValidationResult(true),
                InputValidationResult(false),
                InputValidationResult(false),
                false
            ),
            emailAddress = "user1@mail.com",
            password = null,
            confirmPassword = null
        )
    }
}