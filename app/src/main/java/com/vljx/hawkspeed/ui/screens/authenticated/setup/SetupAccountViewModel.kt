package com.vljx.hawkspeed.ui.screens.authenticated.setup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.account.CheckName
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleStock
import com.vljx.hawkspeed.domain.requestmodels.account.RequestCheckName
import com.vljx.hawkspeed.domain.requestmodels.account.RequestSetupProfile
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestCreateVehicle
import com.vljx.hawkspeed.domain.usecase.account.CheckNameUseCase
import com.vljx.hawkspeed.domain.usecase.account.SetupAccountProfileUseCase
import com.vljx.hawkspeed.domain.usecase.vehicle.stock.GetVehicleStockFromCacheUseCase
import com.vljx.hawkspeed.ui.component.InputValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SetupAccountViewModel @Inject constructor(
    private val setupAccountProfileUseCase: SetupAccountProfileUseCase,
    private val checkNameUseCase: CheckNameUseCase,
    private val getVehicleStockFromCacheUseCase: GetVehicleStockFromCacheUseCase,

    private val savedStateHandle: SavedStateHandle,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
): ViewModel() {
    /**
     * Read the User's UID from the saved state handle.
     */
    private val userUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_USER_UID]))

    /**
     * Setup a shared flow for the UI state for the setup account function. We will configure this to refrain from replaying emissions.
     */
    private val mutableSetupAccountUiState: MutableSharedFlow<SetupAccountUiState> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    /**
     * Required arguments for setting the profile up; profile image, username and vehicle information.
     */
    // TODO: profile image.
    private val mutableUsername: MutableStateFlow<String?> = MutableStateFlow(null)
    private val mutableBio: MutableStateFlow<String?> = MutableStateFlow(null)
    private val mutableSelectedVehicleStockUid: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * Expose arguments for UI updates.
     */
    val usernameState: StateFlow<String?> = mutableUsername
    val bioState: StateFlow<String?> = mutableBio

    /**
     * A clientside validator for the Username. This is a required precursor to checking availability.
     */
    private val validateUsernameResult: StateFlow<Pair<String?, InputValidationResult>> =
        mutableUsername.map { username ->
            when {
                username.isNullOrBlank() -> Pair(null, InputValidationResult(false))
                else -> Pair(username, InputValidationResult(true))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(null, InputValidationResult(false)))

    /**
     * Bio always validates successfully since it is not required.
     */
    private val validateBioResult: StateFlow<InputValidationResult> =
        mutableBio.map { bio ->
            InputValidationResult(true)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InputValidationResult(false))

    /**
     * Flat map the latest selected vehicle stock UID to to a query for that vehicle stock from cache. Configure this as a state flow with null
     * as the default value, so emissions begin immediately.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val selectedVehicleStock: StateFlow<VehicleStock?> =
        mutableSelectedVehicleStockUid.flatMapLatest { vehicleStockUid ->
            getVehicleStockFromCacheUseCase(vehicleStockUid ?: "")
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * A state that will eventually emit a username status for the currently entered username. The flow must not actually execute until clientside validation for
     * the username succeeds, and even then, should delay for 1.2 seconds, cancelling and restarting this process if the username ever changes. We'll configure
     * this flow as a state flow, since we want it to emit an Idle value straight away.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val usernameStatusUiState: StateFlow<UsernameStatusUiState> =
        validateUsernameResult.flatMapLatest { (latestUsername, validationResult) ->
            flow {
                // Always emit idle when username changes, to indicate we've cancelled previous queries.
                emit(UsernameStatusUiState.Idle)
                // Now, only if validation result is successful will we actually begin out countdown of 1.2 seconds.
                if(validationResult.isValid) {
                    // Initiate countdown for 1.2 seconds.
                    delay(1200)
                    // Emit the checking state.
                    emit(UsernameStatusUiState.QueryingStatus)
                    val checkNameResource: Resource<CheckName> = checkNameUseCase(
                        RequestCheckName(latestUsername!!)
                    )
                    // Now, emit the outcome of the check.
                    emit(
                        when(checkNameResource.status) {
                            Resource.Status.SUCCESS -> {
                                if(checkNameResource.data!!.isTaken) {
                                    UsernameStatusUiState.UsernameTaken(latestUsername)
                                } else {
                                    UsernameStatusUiState.UsernameAvailable(latestUsername)
                                }
                            }
                            Resource.Status.ERROR -> {
                                // TODO: handle errors on check name request.
                                throw NotImplementedError("usernameStatusUiState failed because errors aren't handled on the network request.")
                            }
                            Resource.Status.LOADING -> throw NotImplementedError("checkNameUseCase should never emit a loading status.")
                        }
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UsernameStatusUiState.Idle)

    /**
     * A validator for whether the account can be set up; involving the ultimate result of clientside username validation, whether or not username is taken,
     * validation result of vehicle information and validation result of bio.
     */
    private val canSetupProfile: StateFlow<Boolean> =
        combine(
            validateUsernameResult,
            usernameStatusUiState,
            selectedVehicleStock,
            validateBioResult
        ) { validateUsername, usernameStatus, vehicleStock, validateBio ->
            validateUsername.second.isValid && usernameStatus is UsernameStatusUiState.UsernameAvailable && vehicleStock != null && validateBio.isValid
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * A flow that combines all flows involved in determining the latest state of the form itself. We'll also share this flow.
     */
    private val setupAccountFormUiState: SharedFlow<SetupAccountFormUiState> =
        combine(
            validateUsernameResult,
            usernameStatusUiState,
            selectedVehicleStock,
            validateBioResult,
            canSetupProfile
        ) { validateUsername, usernameStatus, vehicleStock, validateBio, canSetup ->
            SetupAccountFormUiState.SetupAccountForm(
                validateUsername.second,
                usernameStatus,
                vehicleStock,
                validateBio,
                canSetup
            )
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    /**
     * Now, merge our mutable setup account shared flow and the account form UI state above, mapping the latter to a state to show the form.
     */
    val setupAccountUiState: StateFlow<SetupAccountUiState> =
        merge(
            setupAccountFormUiState.map { uiState ->
                SetupAccountUiState.ShowSetupAccountForm(
                    uiState
                )
            },
            mutableSetupAccountUiState
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SetupAccountUiState.Loading)

    /**
     * Set this account's profile up.
     */
    fun setupAccountProfile() {
        viewModelScope.launch(ioDispatcher) {
            // Get all required arguments.
            val username: String = mutableUsername.value
                ?: throw NotImplementedError("Failed to setupAccountProfile(), username can't be null.")
            val selectedVehicleStockUid: String = mutableSelectedVehicleStockUid.value
                ?: throw NotImplementedError("Failed to setupAccountProfile(), vehicle stock uid can't be null.")
            val bio: String? = mutableBio.value

            // Now, perform the creation request, emitting all results to our mutable shared state.
            mutableSetupAccountUiState.emitAll(
                setupAccountProfileUseCase(
                    RequestSetupProfile(username, RequestCreateVehicle(selectedVehicleStockUid), bio)
                )
                    .flowOn(ioDispatcher)
                    .map { accountResource ->
                        when(accountResource.status) {
                            Resource.Status.SUCCESS -> SetupAccountUiState.AccountSetup(accountResource.data!!)
                            Resource.Status.LOADING -> SetupAccountUiState.ShowSetupAccountForm(
                                SetupAccountFormUiState.SettingUp
                            )
                            Resource.Status.ERROR -> SetupAccountUiState.ShowSetupAccountForm(
                                SetupAccountFormUiState.SetupAccountFailed(accountResource.resourceError!!)
                            )
                        }
                    }
            )
        }
    }

    /**
     * Update the chosen username.
     */
    fun updateUsername(username: String) {
        mutableUsername.value = username
    }

    /**
     * Update the chosen bio.
     */
    fun updateBio(bio: String) {
        mutableBio.value = bio
    }

    /**
     * Update the chosen vehicle stock UID.
     */
    fun selectVehicleStockUid(vehicleStockUid: String) {
        mutableSelectedVehicleStockUid.value = vehicleStockUid
    }

    /**
     * Clear the selected vehicle.
     */
    fun clearSelectedVehicle() {
        mutableSelectedVehicleStockUid.value = null
    }

    companion object {
        const val ARG_USER_UID = "userUid"
    }
}