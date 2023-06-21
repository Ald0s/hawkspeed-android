package com.vljx.hawkspeed.ui.screens.authenticated.verify

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.usecase.account.GetAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyAccountViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getAccountUseCase: GetAccountUseCase

): ViewModel() {
    /**
     * Read the User's UID from the saved state handle.
     */
    private val userUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_USER_UID]))

    /**
     * Setup a mutable shared flow so that updates to the UI can be fed here.
     */
    private val mutableVerifyAccountUiState: MutableSharedFlow<VerifyAccountUiState> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    /**
     * Publicise the shared flow here.
     */
    val verifyAccountUiState: SharedFlow<VerifyAccountUiState> =
        mutableVerifyAccountUiState

    /**
     * Perform a query for the current User's account, and emit the result to our UI state.
     */
    suspend fun refreshAccount() {
        // Emit a loading state.
        mutableVerifyAccountUiState.emit(VerifyAccountUiState.Loading)
        // Perform a query for the current account.
        val accountResource: Resource<Account> = getAccountUseCase(Unit)
        // Now, emit the result of this resource to the UI state.
        mutableVerifyAccountUiState.emit(
            when(accountResource.status) {
                Resource.Status.SUCCESS -> {
                    if(accountResource.data!!.isAccountVerified) {
                        VerifyAccountUiState.AccountVerified(accountResource.data!!)
                    } else {
                        VerifyAccountUiState.AccountNotVerified(accountResource.data!!)
                    }
                }
                Resource.Status.LOADING -> VerifyAccountUiState.Loading
                Resource.Status.ERROR -> VerifyAccountUiState.Failed(accountResource.resourceError!!)
            }
        )
    }

    companion object {
        const val ARG_USER_UID = "userUid"
    }
}