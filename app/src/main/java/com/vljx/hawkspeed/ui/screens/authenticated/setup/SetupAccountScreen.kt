package com.vljx.hawkspeed.ui.screens.authenticated.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

/**
 * TODO: validation must still be applied to all form controls.
 */
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        OutlinedTextField(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "username"
                                )
                            },
                            value = username ?: "",
                            placeholder = {
                                Text(text = stringResource(id = R.string.placeholder_username))
                            },
                            onValueChange = updateUsername,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                    UsernameAvailability(
                        usernameStatusUi = usernameStatusUi,
                        modifier = Modifier
                            .weight(0.2f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "vehicle information"
                        )
                    },
                    value = vehicleInformation ?: "",
                    onValueChange = updateVehicleInformation,
                    placeholder = {
                        Text(text = stringResource(id = R.string.placeholder_vehicle))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    singleLine = false,
                    maxLines = 3,
                    value = bio ?: "",
                    onValueChange = updateBio,
                    placeholder = {
                        Text(text = stringResource(id = R.string.placeholder_bio))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onSetupProfileClicked,
                    enabled = canSetupProfile,
                    shape = RectangleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.setup_account_setup).uppercase())
                }
            }
        }
    }
}

@Composable
fun UsernameAvailability(
    usernameStatusUi: UsernameStatusUiState,
    modifier: Modifier = Modifier
) {
    if(usernameStatusUi !is UsernameStatusUiState.Idle) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
        ) {
            when (usernameStatusUi) {
                is UsernameStatusUiState.QueryingStatus -> {
                    // Display a small circular progress indicator.
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(32.dp)
                    )
                }

                is UsernameStatusUiState.UsernameAvailable -> {
                    // Display its available.
                    Image(
                        painter = painterResource(id = R.drawable.baseline_check_24),
                        modifier = Modifier
                            .size(32.dp),
                        contentDescription = "available",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }

                is UsernameStatusUiState.UsernameTaken -> {
                    // Display its taken.
                    Image(
                        painter = painterResource(id = R.drawable.baseline_close_24),
                        modifier = Modifier
                            .size(32.dp),
                        contentDescription = "available",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error)
                    )
                }

                else -> { }
            }
        }
    }
}

@Preview
@Composable
fun PreviewSetupAccountFrom(

) {
    HawkSpeedTheme {
        SetupAccountFormUi(
            onSetupProfileClicked = { /*TODO*/ },
            username = "aldos",
            validateUsernameResult = InputValidationResult(true),
            updateUsername = { e -> },
            usernameStatusUi = UsernameStatusUiState.QueryingStatus,
            bio = "",
            validateBioResult = InputValidationResult(true),
            updateBio = { e -> },
            vehicleInformation = "",
            validateVehicleInformationResult = InputValidationResult(true),
            updateVehicleInformation = { e -> },
            canSetupProfile = true
        )
    }
}