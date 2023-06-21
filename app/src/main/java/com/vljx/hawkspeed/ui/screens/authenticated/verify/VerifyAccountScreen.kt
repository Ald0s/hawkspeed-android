package com.vljx.hawkspeed.ui.screens.authenticated.verify

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.vljx.hawkspeed.domain.models.account.Account
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
        is VerifyAccountUiState.AccountNotVerified -> {
            AccountNotVerified(
                account = (verifyAccountUiState as VerifyAccountUiState.AccountNotVerified).account
            )
        }
        is VerifyAccountUiState.Loading -> {
            // TODO: some loading indicator here.
        }
        is VerifyAccountUiState.Failed -> {
            // TODO: Failed to verify account UI state.
            throw NotImplementedError("Failed to verify account UI state!")
        }
    }

    // Create a new disposable side effect that will execute the check account function everytime lifecycle starts.
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val startedObserver = LifecycleEventObserver { _, event ->
            // If event is ON_START, call out to view model to refresh the current account.
            if(event == Lifecycle.Event.ON_START) {
                lifecycleOwner.lifecycleScope.launch {
                    verifyAccountViewModel.refreshAccount()
                }
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

@Composable
fun AccountNotVerified(
    account: Account
) {
    Text(text = "Account not verified")
}