package com.vljx.hawkspeed.ui.screens.authenticated.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.ui.component.InputValidationResult
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme

@Composable
fun SetupAccountScreen(
    onAccountSetup: (Account) -> Unit,
    setupAccountViewModel: SetupAccountViewModel = hiltViewModel()
) {
    val currentOnAccountSetup by rememberUpdatedState(onAccountSetup)
    val setupAccountUiState: SetupAccountUiState by setupAccountViewModel.setupAccountUiState.collectAsState(
        initial = SetupAccountUiState.Idle
    )

    when(setupAccountUiState) {
        is SetupAccountUiState.AccountSetup -> {
            // Account has been set up, we'll invoke our callback on a launched side effect.
            LaunchedEffect(key1 = Unit, block = {
                currentOnAccountSetup((setupAccountUiState as SetupAccountUiState.AccountSetup).account)
            })
        }
        is SetupAccountUiState.Failed -> {
            // TODO: proper error handling here.
            throw NotImplementedError("Failed to setup account UI state, please handle this.")
        }
        is SetupAccountUiState.Loading -> {
            // TODO: some loading indicator here.
        }
        is SetupAccountUiState.Idle -> {
            // There is nothing to do for idle.
        }
    }

    val username: String? by setupAccountViewModel.username.collectAsState()
    val validateUsernameResult: InputValidationResult by setupAccountViewModel.validateUsernameResult.collectAsState()
    val usernameStatusUi: UsernameStatusUiState by setupAccountViewModel.usernameStatusUiState.collectAsState(
        initial = UsernameStatusUiState.Idle
    )
    val bio: String? by setupAccountViewModel.bio.collectAsState()
    val validateBioResult: InputValidationResult by setupAccountViewModel.validateBioResult.collectAsState()
    val vehicleInformation: String? by setupAccountViewModel.vehicleInformation.collectAsState()
    val validateVehicleInformationResult: InputValidationResult by setupAccountViewModel.validateVehicleInformationResult.collectAsState()
    val canSetupProfile: Boolean by setupAccountViewModel.canSetupProfile.collectAsState()

    SetupAccountFormUi(
        onSetupProfileClicked = setupAccountViewModel::setupAccountProfile,
        username = username,
        validateUsernameResult = validateUsernameResult,
        updateUsername = setupAccountViewModel::updateUsername,
        usernameStatusUi = usernameStatusUi,
        bio = bio,
        validateBioResult = validateBioResult,
        updateBio = setupAccountViewModel::updateBio,
        vehicleInformation = vehicleInformation,
        validateVehicleInformationResult = validateVehicleInformationResult,
        updateVehicleInformation = setupAccountViewModel::updateVehicleInformation,
        canSetupProfile = canSetupProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupAccountFormUi(
    onSetupProfileClicked: () -> Unit,

    username: String?,
    validateUsernameResult: InputValidationResult,
    updateUsername: (String) -> Unit,
    usernameStatusUi: UsernameStatusUiState,

    bio: String?,
    validateBioResult: InputValidationResult,
    updateBio: (String) -> Unit,

    vehicleInformation: String?,
    validateVehicleInformationResult: InputValidationResult,
    updateVehicleInformation: (String) -> Unit,
    canSetupProfile: Boolean
) {
    Scaffold { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
        ) {
            Row {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    OutlinedTextField(
                        value = username ?: "",
                        onValueChange = updateUsername
                    )
                }

                Column (
                    modifier = Modifier
                        .weight(1f)
                ){
                    when(usernameStatusUi) {
                        is UsernameStatusUiState.QueryingStatus -> {
                            // Display a small circular progress indicator.
                            CircularProgressIndicator()
                        }
                        is UsernameStatusUiState.UsernameAvailable -> {
                            // Display its available.
                            Text(text = "available")
                        }
                        is UsernameStatusUiState.UsernameTaken -> {
                            // Display its taken.
                            Text(text = "taken")
                        }
                        is UsernameStatusUiState.Idle -> {
                            // Display nothing.
                        }
                    }
                }
            }

            OutlinedTextField(
                value = bio ?: "",
                onValueChange = updateBio
            )
            OutlinedTextField(
                value = vehicleInformation ?: "",
                onValueChange = updateVehicleInformation
            )

            Button(
                onClick = onSetupProfileClicked,
                enabled = canSetupProfile
            ) {
                Text(text = stringResource(id = R.string.setup_account_setup))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupAccountFrom(

) {
    HawkSpeedTheme {
        SetupAccountFormUi(
            onSetupProfileClicked = { /*TODO*/ },
            username = "User1",
            validateUsernameResult = InputValidationResult(true),
            updateUsername = { e -> },
            usernameStatusUi = UsernameStatusUiState.UsernameAvailable("User1"),
            bio = "This is a bio",
            validateBioResult = InputValidationResult(true),
            updateBio = { e -> },
            vehicleInformation = "1994 Toyota Supra",
            validateVehicleInformationResult = InputValidationResult(true),
            updateVehicleInformation = { e -> },
            canSetupProfile = true
        )
    }
}