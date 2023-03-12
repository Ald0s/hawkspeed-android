package com.vljx.hawkspeed.viewmodel.onboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.interactor.account.RegisterLocalAccountUseCase
import com.vljx.hawkspeed.domain.models.account.Registration
import com.vljx.hawkspeed.domain.requests.RegisterLocalAccountRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerLocalAccountUseCase: RegisterLocalAccountUseCase
): ViewModel() {
    /**
     * A mutable shared flow for the registration result.
     */
    private val mutableRegistrationResult: MutableSharedFlow<Resource<Registration>> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    val registrationResult: Flow<Resource<Registration>> =
        mutableRegistrationResult.distinctUntilChanged()

    val mutableEmailAddress: MutableStateFlow<String?> = MutableStateFlow(null)
    val mutablePassword: MutableStateFlow<String?> = MutableStateFlow(null)
    val mutableConfirmPassword: MutableStateFlow<String?> = MutableStateFlow(null)

    private val isEmailAddressValid: Flow<Boolean> =
        mutableEmailAddress.map { emailAddress ->
            if(emailAddress.isNullOrBlank()) {
                return@map false
            }
            true
        }

    private val isPasswordValid: Flow<Boolean> =
        mutablePassword.map { password ->
            if(password.isNullOrBlank()) {
                return@map false
            }
            true
        }

    private val isPasswordConfirmed: Flow<Boolean> =
        mutablePassword.combine(mutableConfirmPassword) { password, confirmPassword ->
            if(password != confirmPassword) {
                return@combine false
            }
            true
        }

    val canRegister: StateFlow<Boolean> =
        combine(
            isEmailAddressValid,
            isPasswordValid,
            isPasswordConfirmed
        ) { emailValid, passwordValid, passwordConfirmed ->
            emailValid && passwordValid && passwordConfirmed
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val registerLocalAccountRequest: StateFlow<RegisterLocalAccountRequest?> =
        combine(
            mutableEmailAddress,
            mutablePassword,
            mutableConfirmPassword
        ) { emailAddress, password, confirmPassword ->
            if(emailAddress != null && password != null && confirmPassword != null) {
                return@combine RegisterLocalAccountRequest(emailAddress, password, confirmPassword)
            } else{
                return@combine null
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Attempt the registration of a new account.
     */
    fun attemptRegistration(registerLocalAccountRequest: RegisterLocalAccountRequest) {
        viewModelScope.launch {
            mutableRegistrationResult.emitAll(
                registerLocalAccountUseCase(registerLocalAccountRequest)
            )
        }
    }
}