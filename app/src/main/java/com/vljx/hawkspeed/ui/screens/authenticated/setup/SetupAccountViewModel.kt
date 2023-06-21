package com.vljx.hawkspeed.ui.screens.authenticated.setup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.account.CheckName
import com.vljx.hawkspeed.domain.requestmodels.account.RequestCheckName
import com.vljx.hawkspeed.domain.requestmodels.account.RequestSetupProfile
import com.vljx.hawkspeed.domain.usecase.account.CheckNameUseCase
import com.vljx.hawkspeed.domain.usecase.account.SetupAccountProfileUseCase
import com.vljx.hawkspeed.ui.component.InputValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupAccountViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val setupAccountProfileUseCase: SetupAccountProfileUseCase,
    private val checkNameUseCase: CheckNameUseCase
): ViewModel() {
    /**
     * Read the User's UID from the saved state handle.
     */
    private val userUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_USER_UID]))

    /**
     * Required arguments for setting the profile up; profile image, username and vehicle information.
     */
    // TODO: profile image.
    private val mutableUsername: MutableStateFlow<String?> = MutableStateFlow(null)
    private val mutableBio: MutableStateFlow<String?> = MutableStateFlow(null)
    private val mutableVehicleInformation: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * Publicise all arguments.
     */
    val username: StateFlow<String?> =
        mutableUsername
    val bio: StateFlow<String?> =
        mutableBio
    val vehicleInformation: StateFlow<String?> =
        mutableVehicleInformation

    /**
     * Flat map the latest emission from the username flow, delay for 1.2 seconds, then perform a request for whether the username is already taken or not,
     * emitting the relevant state depending on the outcome.
     */
    val usernameStatusUiState: Flow<UsernameStatusUiState> =
        mutableUsername.flatMapLatest { latestUsername ->
            flow {
                // Always emit idle when username changes, to indicate we've cancelled previous queries.
                emit(UsernameStatusUiState.Idle)
                // If username is null or blank, we will return null.
                if(latestUsername.isNullOrBlank()) {
                    emit(UsernameStatusUiState.Idle)
                } else {
                    // Otherwise, wait for 1.2 seconds, then perform a check name request.
                    delay(1200)
                    // Emit the checking state.
                    emit(UsernameStatusUiState.QueryingStatus)
                    val checkNameResource: Resource<CheckName> = checkNameUseCase(
                        RequestCheckName(latestUsername)
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
        }

    /**
     * Validators for required arguments.
     * Starting with the validator for the username; this will combine the outcomes of both a local validator to ensure the username given can be used,
     * and the outcome of checking whether that username is already taken.
     */
    val validateUsernameResult: StateFlow<InputValidationResult> =
        combine(
            mutableUsername.map { username ->
                // TODO: more complex validation.
                InputValidationResult(
                    !username.isNullOrBlank()
                )
            },
            usernameStatusUiState
        ) { validationResult, usernameStatus ->
            InputValidationResult(
                validationResult.isValid && usernameStatus is UsernameStatusUiState.UsernameAvailable
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, InputValidationResult(false))

    /**
     * Bio always validates successfully since it is not required.
     */
    val validateBioResult: StateFlow<InputValidationResult> =
        mutableBio.map { bio ->
            InputValidationResult(true)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, InputValidationResult(true))

    val validateVehicleInformationResult: StateFlow<InputValidationResult> =
        mutableVehicleInformation.map { vehicleInfo ->
            // TODO: more complex validation.
            InputValidationResult(
                !vehicleInfo.isNullOrBlank()
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, InputValidationResult(false))

    /**
     * A validator for whether the account can be set up.
     */
    val canSetupProfile: StateFlow<Boolean> =
        combine(
            validateUsernameResult,
            validateVehicleInformationResult,
            validateBioResult
        ) { username, vehicle, bio ->
            username.isValid && vehicle.isValid && bio.isValid
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Setup a shared flow for the UI state for the setup account function. We will configure this to refrain from replaying emissions.
     */
    private val mutableSetupAccountUiState: MutableSharedFlow<SetupAccountUiState> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    /**
     * Publicise this setup account UI state.
     */
    val setupAccountUiState: SharedFlow<SetupAccountUiState> =
        mutableSetupAccountUiState

    /**
     * Set this account's profile up.
     */
    fun setupAccountProfile() {
        viewModelScope.launch {
            // Get all required arguments.
            val username: String = mutableUsername.value
                ?: throw NotImplementedError("Failed to setupAccountProfile(), username can't be null.")
            val vehicleInformation: String = mutableVehicleInformation.value
                ?: throw NotImplementedError("Failed to setupAccountProfile(), vehicle info can't be null.")
            val bio: String? = mutableBio.value

            // Now, perform the creation request, emitting all results to our mutable shared state.
            mutableSetupAccountUiState.emitAll(
                setupAccountProfileUseCase(
                    RequestSetupProfile(username, vehicleInformation, bio)
                ).map { accountResource ->
                    when(accountResource.status) {
                        Resource.Status.SUCCESS -> SetupAccountUiState.AccountSetup(accountResource.data!!)
                        Resource.Status.LOADING -> SetupAccountUiState.Loading
                        Resource.Status.ERROR -> SetupAccountUiState.Failed(accountResource.resourceError!!)
                    }
                }
            )
        }
    }

    fun updateUsername(username: String) {
        mutableUsername.value = username
    }

    fun updateBio(bio: String) {
        mutableBio.value = bio
    }

    fun updateVehicleInformation(vehicleInformation: String) {
        mutableVehicleInformation.value = vehicleInformation
    }

    companion object {
        const val ARG_USER_UID = "userUid"
    }
}