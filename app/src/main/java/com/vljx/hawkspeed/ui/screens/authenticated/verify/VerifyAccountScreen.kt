package com.vljx.hawkspeed.ui.screens.authenticated.verify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData
import kotlinx.coroutines.launch

@Composable
fun VerifyAccountScreen(
    onAccountVerified: (Account) -> Unit,
    verifyAccountViewModel: VerifyAccountViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val currentOnAccountVerified by rememberUpdatedState(onAccountVerified)
    val verifyAccountUiState: VerifyAccountUiState by verifyAccountViewModel.verifyAccountUiState.collectAsState(
        initial = VerifyAccountUiState.Loading
    )

    when(verifyAccountUiState) {
        is VerifyAccountUiState.AccountVerified -> {
            // When account is verified, we'll invoke our callback from a launched side effect.
            LaunchedEffect(key1 = Unit, block = {
                currentOnAccountVerified((verifyAccountUiState as VerifyAccountUiState.AccountVerified).account)
            })
        }
        is VerifyAccountUiState.AccountNotVerified, is VerifyAccountUiState.Loading, is VerifyAccountUiState.Failed -> {
            // Can we resend verification email?
            val canResendEmail by verifyAccountViewModel.canResendVerificationEmail.collectAsState()
            // For all other outcomes, call the composable.
            VerifyAccountForm(
                onResendEmailClicked = verifyAccountViewModel::resendVerificationEmail,
                canResendEmail = canResendEmail,
                verifyAccountUiState = verifyAccountUiState
            )
        }
    }
    // Create a new disposable side effect that will execute the check account function everytime lifecycle starts.
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val startedObserver = LifecycleEventObserver { _, event ->
            // If event is ON_START, call out to view model to refresh the current account.
            if(event == Lifecycle.Event.ON_START) {
                verifyAccountViewModel.refreshAccount()
            }
        }
        // Add observer to lifecycle.
        lifecycleOwner.lifecycle.addObserver(startedObserver)
        onDispose {
            // Remove observer.
            lifecycleOwner.lifecycle.removeObserver(startedObserver)
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyAccountForm(
    onResendEmailClicked: () -> Unit,
    canResendEmail: Boolean,
    verifyAccountUiState: VerifyAccountUiState
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
                when(verifyAccountUiState) {
                    is VerifyAccountUiState.AccountNotVerified -> {
                        // Account is not verified just yet, so display the option to resend email, if allowed by the state.
                        Text(
                            text = stringResource(id = R.string.verify_account_not_verified_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = R.string.verify_account_not_verified_body),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(56.dp))
                        Button(
                            onClick = onResendEmailClicked,
                            enabled = canResendEmail,
                            shape = RectangleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(text = stringResource(id = R.string.verify_resend_email).uppercase())
                        }
                    }
                    is VerifyAccountUiState.Failed -> {
                        // Failed to check account is verified.
                        Text(
                            text = stringResource(id = R.string.verify_account_verify_failed_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = R.string.verify_account_verify_failed_body),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is VerifyAccountUiState.Loading -> {
                        // The verification is loading.
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            style = MaterialTheme.typography.titleMedium,
                            text = stringResource(id = R.string.verify_loading)
                        )
                    }
                    is VerifyAccountUiState.AccountVerified -> {
                        // This is not handled here.
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAccountNotVerified(

) {
    HawkSpeedTheme {
        VerifyAccountForm(
            onResendEmailClicked = {},
            canResendEmail = false,
            verifyAccountUiState = VerifyAccountUiState.AccountNotVerified(ExampleData.getExampleAccount())
        )
    }
}