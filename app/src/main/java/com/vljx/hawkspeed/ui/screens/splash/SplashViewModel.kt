package com.vljx.hawkspeed.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.usecase.account.CheckLoginUseCase
import com.vljx.hawkspeed.domain.models.account.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkLoginUseCase: CheckLoginUseCase
): ViewModel() {
    /**
     * Setup a flow for the result of attempting to login. This will, when collect, try and reauthenticate the current login with the server
     * and emit the outcome.
     */
    private val accountResource: Flow<Resource<Account>> =
        checkLoginUseCase(Unit)

    /**
     * Map the outcome of the above query to a state of the appropriate type. We'll make this a state flow, so that the latest value emitted will
     * always be replayed. This isn't a big issue because the splash screen should only ever be returned to in the case of a total app restart.
     */
    val splashUiState: StateFlow<SplashUiState> =
        accountResource.map { accountResource ->
            when(accountResource.status) {
                Resource.Status.SUCCESS -> SplashUiState.SuccessfulAuthentication(accountResource.data!!)
                Resource.Status.LOADING -> SplashUiState.Loading
                Resource.Status.ERROR -> SplashUiState.Failed(accountResource.resourceError!!)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SplashUiState.Loading)
}