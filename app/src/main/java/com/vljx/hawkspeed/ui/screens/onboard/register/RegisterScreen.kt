package com.vljx.hawkspeed.ui.screens.onboard.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Registration
import com.vljx.hawkspeed.ui.component.InputValidationResult
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistered: (Registration) -> Unit,
    registerViewModel: RegisterViewModel = hiltViewModel()
) {
    val registerUiState by registerViewModel.registerUiState.collectAsState(initial = RegisterUiState.Idle)

    when(registerUiState) {
        is RegisterUiState.RegistrationSuccessful -> {
            // When registration succeeds, we'll invoke our callback from a launched side effect.
            LaunchedEffect(key1 = Unit, block = {
                onRegistered((registerUiState as RegisterUiState.RegistrationSuccessful).registration)
            })
        }
        is RegisterUiState.Loading -> {
            // TODO: show a loading indicator
        }
        is RegisterUiState.RegistrationFailed -> {
            // TODO: handle registration failed case.
        }
        is RegisterUiState.Idle -> {
            // Don't have to do anything for idling.
        }
    }

    val emailAddress: String? by registerViewModel.emailAddress.collectAsState()
    val validateEmailAddressResult by registerViewModel.validateEmailAddressResult.collectAsState()
    val password: String? by registerViewModel.password.collectAsState()
    val validatePasswordResult by registerViewModel.validatePasswordResult.collectAsState()
    val confirmPassword: String? by registerViewModel.confirmPassword.collectAsState()
    val validateConfirmPasswordResult by registerViewModel.validateConfirmPasswordResult.collectAsState()

    RegisterFormUi(
        onAttemptRegistrationClicked = { /*TODO*/ },
        emailAddress = emailAddress,
        validateEmailAddressResult = validateEmailAddressResult,
        updateEmailAddress = registerViewModel::updateEmailAddress,
        password = password,
        validatePasswordResult = validatePasswordResult,
        updatePassword = registerViewModel::updatePassword,
        confirmPassword = confirmPassword,
        validateConfirmPasswordResult = validateConfirmPasswordResult,
        updateConfirmPassword = registerViewModel::updateConfirmPassword
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterFormUi(
    onAttemptRegistrationClicked: () -> Unit,

    emailAddress: String?,
    validateEmailAddressResult: InputValidationResult,
    updateEmailAddress: (String) -> Unit,

    password: String?,
    validatePasswordResult: InputValidationResult,
    updatePassword: (String) -> Unit,

    confirmPassword: String?,
    validateConfirmPasswordResult: InputValidationResult,
    updateConfirmPassword: (String) -> Unit,

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
            OutlinedTextField(
                value = confirmPassword ?: "",
                onValueChange = updateConfirmPassword
            )
            Button(
                onClick = onAttemptRegistrationClicked
            ) {
                Text(text = "Register")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterFormUi(

) {
    HawkSpeedTheme {
        RegisterFormUi(
            onAttemptRegistrationClicked = { /*TODO*/ },
            emailAddress = "user1@mail.com",
            validateEmailAddressResult = InputValidationResult(true),
            updateEmailAddress = { e -> },
            password = "password",
            validatePasswordResult = InputValidationResult(true),
            updatePassword = { e -> },
            confirmPassword = "password",
            validateConfirmPasswordResult = InputValidationResult(true),
            updateConfirmPassword = { e -> }
        )
    }
}