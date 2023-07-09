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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.ui.component.InputValidationResult
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme

@Composable
fun SetupAccountScreen(
    onAccountSetup: (Account) -> Unit,

    setupAccountViewModel: SetupAccountViewModel = hiltViewModel()
) {
    val setupAccountUiState: SetupAccountUiState by setupAccountViewModel.setupAccountUiState.collectAsState()
    when(setupAccountUiState) {
        is SetupAccountUiState.AccountSetup -> {
            // Account has been set up, we'll invoke our callback on a launched side effect.
            LaunchedEffect(key1 = Unit, block = {
                onAccountSetup((setupAccountUiState as SetupAccountUiState.AccountSetup).account)
            })
            // Show the loading composable.
            LoadingScreen()
        }
        is SetupAccountUiState.ShowSetupAccountForm -> {
            // Show the setup account form UI.
            val usernameState: String? by setupAccountViewModel.usernameState.collectAsState()
            val bioState: String? by setupAccountViewModel.bioState.collectAsState()
            val vehicleInformationState: String? by setupAccountViewModel.vehicleInformationState.collectAsState()

            SetupAccountFormUi(
                showSetupAccountForm = setupAccountUiState as SetupAccountUiState.ShowSetupAccountForm,
                username = usernameState,
                bio = bioState,
                vehicleInformation = vehicleInformationState,

                updateUsername = setupAccountViewModel::updateUsername,
                updateBio = setupAccountViewModel::updateBio,
                updateVehicleInformation = setupAccountViewModel::updateVehicleInformation,
                onSetupProfileClicked = setupAccountViewModel::setupAccountProfile
            )
        }
        is SetupAccountUiState.Loading ->
            LoadingScreen()
    }
}

/**
 * TODO: validation must still be applied to all form controls.
 */
@Composable
fun SetupAccountFormUi(
    showSetupAccountForm: SetupAccountUiState.ShowSetupAccountForm,

    username: String?,
    bio: String?,
    vehicleInformation: String?,

    updateUsername: ((String) -> Unit)? = null,
    updateBio: ((String) -> Unit)? = null,
    updateVehicleInformation: ((String) -> Unit)? = null,

    onSetupProfileClicked: (() -> Unit)? = null,
) {
    var usernameStatusUiState: UsernameStatusUiState by remember { mutableStateOf(UsernameStatusUiState.Idle) }
    var canAttemptSetup: Boolean by remember { mutableStateOf(false) }
    var isSettingUp: Boolean by remember { mutableStateOf(false) }
    var setupError: ResourceError? by remember { mutableStateOf(null) }
    when(val setupAccountFormUiState = showSetupAccountForm.setupAccountFormUiState) {
        is SetupAccountFormUiState.SetupAccountForm -> {
            usernameStatusUiState = setupAccountFormUiState.usernameStatusUiState
            canAttemptSetup = setupAccountFormUiState.canAttemptSetupAccount
            isSettingUp = false
        }
        is SetupAccountFormUiState.SettingUp -> {
            setupError = null
            isSettingUp = true
        }
        is SetupAccountFormUiState.SetupAccountFailed -> {
            setupError = setupAccountFormUiState.resourceError
            isSettingUp = false
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
                            onValueChange = updateUsername ?: { },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                    UsernameAvailability(
                        usernameStatusUi = usernameStatusUiState,
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
                    onValueChange = updateVehicleInformation ?: { },
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
                    onValueChange = updateBio ?: { },
                    placeholder = {
                        Text(text = stringResource(id = R.string.placeholder_bio))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onSetupProfileClicked ?: { },
                    enabled = canAttemptSetup && !isSettingUp,
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

                is UsernameStatusUiState.Idle -> {
                    /* Nothing to do. */
                }
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
            username = "aldos",
            bio = "Hey check me out!",
            vehicleInformation = null,
            showSetupAccountForm = SetupAccountUiState.ShowSetupAccountForm(
                setupAccountFormUiState = SetupAccountFormUiState.SetupAccountForm(
                    InputValidationResult(true),
                    UsernameStatusUiState.UsernameAvailable("aldos"),
                    InputValidationResult(false),
                    InputValidationResult(true),
                    false
                )
            )
        )
    }
}