package com.vljx.hawkspeed.ui.screens.onboard.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.account.RequestRegisterLocalAccount
import com.vljx.hawkspeed.domain.usecase.account.RegisterLocalAccountUseCase
import com.vljx.hawkspeed.ui.component.InputValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerLocalAccountUseCase: RegisterLocalAccountUseCase
): ViewModel() {
    /**
     * The result flow for the registration attempt. We've configured this as a shared flow that does not replay values, to stop emissions if this page
     * is navigated back to from somewhere else.
     */
    private val mutableRegisterUiState: MutableSharedFlow<RegisterUiState> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    /**
     * The User's email address.
     */
    private val mutableEmailAddress: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * The User's desired password.
     */
    private val mutablePassword: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * The User's desired password again.
     */
    private val mutableConfirmPassword: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * Publicise all arguments.
     */
    val emailAddress: StateFlow<String?> =
        mutableEmailAddress

    val password: StateFlow<String?> =
        mutablePassword

    val confirmPassword: StateFlow<String?> =
        mutableConfirmPassword

    /**
     * Publicise the UI state flow.
     */
    val registerUiState: SharedFlow<RegisterUiState> =
        mutableRegisterUiState

    /**
     * A validator result for the email address.
     */
    val validateEmailAddressResult: StateFlow<InputValidationResult> =
        mutableEmailAddress.map { emailAddress ->
            // TODO: more complex validation.
            InputValidationResult(
                !emailAddress.isNullOrBlank()
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, InputValidationResult(false))

    /**
     * A validator result for the password.
     */
    val validatePasswordResult: StateFlow<InputValidationResult> =
        mutablePassword.map { emailAddress ->
            // TODO: more complex validation.
            InputValidationResult(
                !emailAddress.isNullOrBlank()
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, InputValidationResult(false))

    /**
     * A validator result that combines both password and confirm password to validate confirm password.
     */
    val validateConfirmPasswordResult: StateFlow<InputValidationResult> =
        combine(
            mutablePassword,
            mutableConfirmPassword
        ) { password, confirmPassword ->
            // Simply return true if password and confirm password are both not null and are the same.
            return@combine InputValidationResult(
                password != null && confirmPassword != null && password == confirmPassword
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, InputValidationResult(false))

    /**
     * A state flow that emits true when a registration attempt can take place.
     */
    val canAttemptRegistration: StateFlow<Boolean> =
        combine(
            validateEmailAddressResult,
            validatePasswordResult,
            validateConfirmPasswordResult
        ) { validateEmail, validatePassword, validateConfirmPass ->
            return@combine validateEmail.isValid && validatePassword.isValid && validateConfirmPass.isValid
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Perform a registration attempt.
     */
    fun attemptRegistration() {
        viewModelScope.launch {
            // Change state to loading.
            mutableRegisterUiState.emit(RegisterUiState.Loading)
            // Get all required arguments.
            val emailAddress: String = mutableEmailAddress.value
                ?: throw NotImplementedError("Failed to register, email address can't be null.")
            val password: String = mutablePassword.value
                ?: throw NotImplementedError("Failed to register, password can't be null.")
            val confirmPassword: String = mutableConfirmPassword.value
                ?: throw NotImplementedError("Failed to register, confirm password can't be null.")
            // Now, call out to the register use case, then map and emit all results from that call.
            mutableRegisterUiState.emitAll(
                registerLocalAccountUseCase(
                    RequestRegisterLocalAccount(emailAddress, password, confirmPassword)
                ).map { registrationResource ->
                    when(registrationResource.status) {
                        Resource.Status.SUCCESS -> RegisterUiState.RegistrationSuccessful(registrationResource.data!!)
                        Resource.Status.LOADING -> RegisterUiState.Loading
                        Resource.Status.ERROR -> RegisterUiState.RegistrationFailed(registrationResource.resourceError!!)
                    }
                }
            )
        }
    }

    /**
     * Update the email address.
     */
    fun updateEmailAddress(emailAddress: String) {
        mutableEmailAddress.tryEmit(emailAddress)
    }

    /**
     * Update the password.
     */
    fun updatePassword(password: String) {
        mutablePassword.tryEmit(password)
    }

    /**
     * Update the confirm password.
     */
    fun updateConfirmPassword(confirmPassword: String) {
        mutableConfirmPassword.tryEmit(confirmPassword)
    }
}