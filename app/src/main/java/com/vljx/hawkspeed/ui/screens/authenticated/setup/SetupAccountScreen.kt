package com.vljx.hawkspeed.ui.screens.authenticated.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.ui.component.InputValidationResult

@OptIn(ExperimentalMaterial3Api::class)
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

    // TODO: improve UI here.
    val username: String? by setupAccountViewModel.username.collectAsState()
    val validateUsernameResult: InputValidationResult by setupAccountViewModel.validateUsernameResult.collectAsState()
    val usernameStatusUiState: UsernameStatusUiState by setupAccountViewModel.usernameStatusUiState.collectAsState(
        initial = UsernameStatusUiState.Idle
    )

    val bio: String? by setupAccountViewModel.bio.collectAsState()
    val validateBioResult: InputValidationResult by setupAccountViewModel.validateBioResult.collectAsState()

    val vehicleInformation: String? by setupAccountViewModel.vehicleInformation.collectAsState()
    val validateVehicleInformationResult: InputValidationResult by setupAccountViewModel.validateVehicleInformationResult.collectAsState()

    val canSetupProfile: Boolean by setupAccountViewModel.canSetupProfile.collectAsState()

    Column {
        Row {
            TextField(
                value = username ?: "",
                onValueChange = setupAccountViewModel::updateUsername
            )
            when(usernameStatusUiState) {
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

        TextField(
            value = bio ?: "",
            onValueChange = setupAccountViewModel::updateBio
        )
        TextField(
            value = vehicleInformation ?: "",
            onValueChange = setupAccountViewModel::updateVehicleInformation
        )

        Button(
            onClick = setupAccountViewModel::setupAccountProfile,
            enabled = canSetupProfile
        ) {
            Text(text = "Setup")
        }
    }
}