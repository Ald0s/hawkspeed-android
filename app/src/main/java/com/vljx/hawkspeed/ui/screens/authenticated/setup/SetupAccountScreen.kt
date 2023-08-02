package com.vljx.hawkspeed.ui.screens.authenticated.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleStock
import com.vljx.hawkspeed.ui.component.InputValidationResult
import com.vljx.hawkspeed.ui.screens.authenticated.choosevehicle.ChooseVehicleViewModel.Companion.ARG_VEHICLE_STOCK_UID
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData

@Composable
fun SetupAccountScreen(
    onAccountSetup: (Account) -> Unit,
    onChooseVehicleClicked: () -> Unit,

    navHostController: NavHostController,
    setupAccountViewModel: SetupAccountViewModel = hiltViewModel()
) {
    // We need nav host controller access here because we'll collect from the selected vehicle stock UID state.
    val selectedVehicleStockUidState: State<String?>? = navHostController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>(ARG_VEHICLE_STOCK_UID, null)
        ?.collectAsStateWithLifecycle()
    // Now, whenever we get a non-null value, pass it to the view model as selected vehicle stock UID, then set key to null.
    selectedVehicleStockUidState?.value?.let { selectedVehicleStockUid ->
        setupAccountViewModel.selectVehicleStockUid(selectedVehicleStockUid)
    }
    // Now collect the overall UI state.
    val setupAccountUiState: SetupAccountUiState by setupAccountViewModel.setupAccountUiState.collectAsStateWithLifecycle()
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
            val usernameState: String? by setupAccountViewModel.usernameState.collectAsStateWithLifecycle()
            val bioState: String? by setupAccountViewModel.bioState.collectAsStateWithLifecycle()

            SetupAccountFormUi(
                showSetupAccountForm = setupAccountUiState as SetupAccountUiState.ShowSetupAccountForm,
                username = usernameState,
                bio = bioState,

                updateUsername = setupAccountViewModel::updateUsername,
                updateBio = setupAccountViewModel::updateBio,

                onChooseVehicleClicked = onChooseVehicleClicked,
                onClearSelectedVehicle = setupAccountViewModel::clearSelectedVehicle,
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

    updateUsername: ((String) -> Unit)? = null,
    updateBio: ((String) -> Unit)? = null,

    onChooseVehicleClicked: (() -> Unit)? = null,
    onClearSelectedVehicle: (() -> Unit)? = null,
    onSetupProfileClicked: (() -> Unit)? = null,
) {
    var usernameStatusUiState: UsernameStatusUiState by remember { mutableStateOf(UsernameStatusUiState.Idle) }
    var selectedVehicleStock: VehicleStock? by remember { mutableStateOf(null) }
    var canAttemptSetup: Boolean by remember { mutableStateOf(false) }
    var isSettingUp: Boolean by remember { mutableStateOf(false) }
    var setupError: ResourceError? by remember { mutableStateOf(null) }

    LaunchedEffect(key1 = showSetupAccountForm, block = {
        when(val setupAccountFormUiState = showSetupAccountForm.setupAccountFormUiState) {
            is SetupAccountFormUiState.SetupAccountForm -> {
                usernameStatusUiState = setupAccountFormUiState.usernameStatusUiState
                selectedVehicleStock = setupAccountFormUiState.selectedVehicleStock
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
    })

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
                VehicleInformation(
                    vehicleStock = selectedVehicleStock,
                    onChooseVehicleClicked = onChooseVehicleClicked,
                    onClearSelectedVehicle = onClearSelectedVehicle
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

@Composable
fun VehicleInformation(
    vehicleStock: VehicleStock?,

    onClearSelectedVehicle: (() -> Unit)? = null,
    onChooseVehicleClicked: (() -> Unit)? = null
) {
    Surface(tonalElevation = 5.dp) {
        Column(
            modifier = Modifier
                .clickable {
                    onChooseVehicleClicked?.invoke()
                }
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if(vehicleStock == null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                    ) {
                        Text(
                            text = stringResource(R.string.choose_vehicle),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.1f)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "more")
                    }
                }
            } else {
                Row {
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                    ) {
                        Row {
                            Text(
                                text = stringResource(R.string.selected_vehicle),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Row {
                            Column(
                                modifier = Modifier
                                    .weight(0.9f)
                            ) {
                                Text(
                                    text = vehicleStock.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.1f)
                    ) {
                        IconButton(
                            onClick = onClearSelectedVehicle ?: {}
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.times_circle),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                                contentDescription = "clear"
                            )
                        }
                    }
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
            showSetupAccountForm = SetupAccountUiState.ShowSetupAccountForm(
                setupAccountFormUiState = SetupAccountFormUiState.SetupAccountForm(
                    InputValidationResult(true),
                    UsernameStatusUiState.UsernameAvailable("aldos"),
                    null,
                    InputValidationResult(true),
                    false
                )
            )
        )
    }
}

@Preview
@Composable
fun PreviewVehicleInformation(

) {
    HawkSpeedTheme {
        VehicleInformation(ExampleData.getExampleVehicle().vehicleStock)
    }
}
