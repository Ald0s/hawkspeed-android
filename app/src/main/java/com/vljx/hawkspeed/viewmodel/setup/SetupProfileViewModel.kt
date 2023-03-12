package com.vljx.hawkspeed.viewmodel.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.interactor.account.CheckNameUseCase
import com.vljx.hawkspeed.domain.interactor.account.SetupProfileUseCase
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.account.CheckName
import com.vljx.hawkspeed.domain.requests.CheckNameRequest
import com.vljx.hawkspeed.domain.requests.SetupProfileRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupProfileViewModel @Inject constructor(
    private val checkNameUseCase: CheckNameUseCase,
    private val setupProfileUseCase: SetupProfileUseCase
): ViewModel() {
    /**
     * A shared flow for the result of the setup procedure.
     */
    private val mutableSetupProfileResult: MutableSharedFlow<Resource<Account>> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    val setupProfileResult: Flow<Resource<Account>> =
        mutableSetupProfileResult.distinctUntilChanged()

    val mutableUsername: MutableStateFlow<String?> = MutableStateFlow(null)
    val mutableBio: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * A state flow that will determine if the Username is valid.
     */
    val isUsernameValid: StateFlow<Boolean> =
        mutableUsername.map { userName ->
            userName?.isNotBlank() == true
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * A state flow that will determine if the bio is valid.
     */
    val isBioValid: StateFlow<Boolean> =
        mutableBio.map { bio ->
            bio?.isNotBlank() == true
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val isUsernameAvailableResource: Flow<Resource<CheckName>> =
        mutableUsername.transformLatest { latestUsername ->
            if(latestUsername != null && latestUsername.isNotEmpty()) {
                // Invoke a delay for 1.5 seconds here, this should be cancelled if username text is updated within that time.
                delay(1500)
                // Emit all
                emitAll(checkNameUseCase(CheckNameRequest(latestUsername)))
            }
        }

    /**
     * A state flow for whether the username is currently available or not. When this emits null, the result is currently indeterminate or being determined.
     */
    val isUsernameAvailable: StateFlow<Boolean?> =
        isUsernameAvailableResource.map { checkNameResource ->
            return@map when(checkNameResource.status) {
                Resource.Status.SUCCESS -> {
                    // When successful, emit the isTaken attribute.
                    return@map !checkNameResource.data!!.isTaken
                }
                Resource.Status.LOADING -> null
                // TODO: on ERROR status, we should actually do something like, emit that error to a more global handler.
                Resource.Status.ERROR -> throw NotImplementedError("Handling errors in isUsernameAvailable StateFlow (SetupProfileViewModel) not yet implemented!")
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * A state flow for whether we can complete the setup process.
     */
    val canCompleteProfile: StateFlow<Boolean> =
        combine(
            isUsernameValid,
            isUsernameAvailable,
            isBioValid
        ) { userNameValid, userNameAvailable, bioValid ->
            userNameValid && userNameAvailable == true && bioValid
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * A state flow for the most recent setup profile request.
     */
    val setupProfileRequest: StateFlow<SetupProfileRequest?> =
        combine(
            mutableUsername,
            mutableBio
        ) { userName, bio ->
            if(userName != null && bio != null) {
                return@combine SetupProfileRequest(userName, bio)
            } else {
                return@combine null
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Complete the user's profile.
     */
    fun submitProfile(setupProfileRequest: SetupProfileRequest) {
        viewModelScope.launch {
            mutableSetupProfileResult.emitAll(
                setupProfileUseCase(setupProfileRequest)
            )
        }
    }
}