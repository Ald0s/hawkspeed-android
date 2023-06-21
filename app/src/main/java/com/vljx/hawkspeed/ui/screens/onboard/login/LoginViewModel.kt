package com.vljx.hawkspeed.ui.screens.onboard.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.usecase.account.LoginUseCase
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.requestmodels.account.RequestLogin
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
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
): ViewModel() {
    /**
     * A mutable shared flow that will allow us to control the login screen's UI state. It is also configured against replaying emissions so the User can
     * navigate back to this screen.
     */
    private val mutableLoginUiState: MutableSharedFlow<LoginUiState> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    /**
     * The form arguments here; email address and password.
     */
    private val mutableEmailAddress: MutableStateFlow<String?> = MutableStateFlow(null)
    private val mutablePassword: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * Validation results for the two required arguments.
     */
    val validateEmailAddressResult: StateFlow<InputValidationResult> =
        mutableEmailAddress.map { emailAddress ->
            // TODO: more complex validation.
            InputValidationResult(
                !emailAddress.isNullOrBlank()
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, InputValidationResult(false))

    val validatePasswordResult: StateFlow<InputValidationResult> =
        mutablePassword.map { password ->
            // TODO: more complex validation.
            InputValidationResult(
                !password.isNullOrBlank()
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, InputValidationResult(false))

    /**
     * Publicise the form arguments.
     */
    val emailAddress: StateFlow<String?> =
        mutableEmailAddress
    val password: StateFlow<String?> =
        mutablePassword

    /**
     * A flow that will emit true when a login attempt can be made; that is, the form is valid.
     */
    val canAttemptLogin: StateFlow<Boolean> =
        combine(
            validateEmailAddressResult,
            validatePasswordResult
        ) { emailAddress, password ->
            emailAddress.isValid && password.isValid
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Publicly expose the mutable login UI state as a shared flow.
     */
    val loginUiState: SharedFlow<LoginUiState> =
        mutableLoginUiState

    /**
     * Attempt a login. This will take the given email address and password, and attempt to authenticate with these.
     */
    fun attemptLogin() {
        viewModelScope.launch {
            // Get arguments.
            val emailAddress: String = mutableEmailAddress.value
                ?: throw NotImplementedError("Email address can't be none.")
            val password: String = mutablePassword.value
                ?: throw NotImplementedError("Password can't be none.")

            // Emit logging in.
            mutableLoginUiState.emit(LoginUiState.LoggingIn)
            // Now, call the use case to receive back a flow for a resource of account. We'll inline map this flow to a UI state and emit all that to
            // our mutable login UI state.
            mutableLoginUiState.emitAll(
                loginUseCase(
                    RequestLogin(emailAddress, password, true)
                ).map { value: Resource<Account> ->
                    when(value.status) {
                        Resource.Status.SUCCESS -> LoginUiState.SuccessfulLogin(value.data!!)
                        Resource.Status.LOADING -> LoginUiState.LoggingIn
                        Resource.Status.ERROR -> LoginUiState.Failed(value.resourceError!!)
                    }
                }
            )
        }
    }

    fun updateEmailAddress(emailAddress: String) {
        mutableEmailAddress.value = emailAddress
    }

    fun updatePassword(password: String) {
        mutablePassword.value = password
    }
}