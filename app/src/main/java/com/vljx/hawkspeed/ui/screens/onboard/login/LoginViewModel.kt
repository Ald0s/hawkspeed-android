package com.vljx.hawkspeed.ui.screens.onboard.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.usecase.account.LoginUseCase
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.requestmodels.account.RequestLogin
import com.vljx.hawkspeed.ui.component.InputValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,

    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
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
     * Expose both arguments for UI updates.
     */
    val emailAddressState: StateFlow<String?> = mutableEmailAddress
    val passwordState: StateFlow<String?> = mutablePassword

    /**
     * A validator for the email address.
     */
    private val validateEmailAddressResult: StateFlow<InputValidationResult> =
        mutableEmailAddress.map { emailAddress ->
            // TODO: more complex validation.
            InputValidationResult(
                !emailAddress.isNullOrBlank()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InputValidationResult(false))

    /**
     * A validator for the password.
     */
    private val validatePasswordResult: StateFlow<InputValidationResult> =
        mutablePassword.map { password ->
            // TODO: more complex validation.
            InputValidationResult(
                !password.isNullOrBlank()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InputValidationResult(false))

    /**
     * A flow that will emit true when a login attempt can be made; that is, the form is valid.
     */
    private val canAttemptLogin: StateFlow<Boolean> =
        combine(
            validateEmailAddressResult,
            validatePasswordResult
        ) { emailAddress, password ->
            emailAddress.isValid && password.isValid
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Combine all form related arguments into a form UI state.
     */
    private val loginFormUiState: SharedFlow<LoginFormUiState> =
        combine(
            validateEmailAddressResult,
            validatePasswordResult,
            canAttemptLogin
        ) { validateEmail, validatePass, canAttempt ->
            LoginFormUiState.LoginForm(
                validateEmail,
                validatePass,
                canAttempt
            )
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    /**
     * Now, merge a mapping of the mutable login UI state shared flow and also a mapped login form UI state. This is publicly available.
     */
    val loginUiState: StateFlow<LoginUiState> =
        merge(
            loginFormUiState.map { formUiState ->
                LoginUiState.ShowLoginForm(
                    formUiState
                )
            },
            mutableLoginUiState
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoginUiState.Loading)

    /**
     * Attempt a login. This will take the given email address and password, and attempt to authenticate with these.
     */
    fun attemptLogin() {
        viewModelScope.launch(ioDispatcher) {
            // Get arguments.
            val emailAddress: String = mutableEmailAddress.value
                ?: throw NotImplementedError("Email address can't be none.")
            val password: String = mutablePassword.value
                ?: throw NotImplementedError("Password can't be none.")
            // Now, call the use case to receive back a flow for a resource of account. We'll inline map this flow to a UI state and emit all that to
            // our mutable login UI state.
            mutableLoginUiState.emitAll(
                loginUseCase(
                    RequestLogin(emailAddress, password, true)
                )
                    .flowOn(ioDispatcher)
                    .map { value: Resource<Account> ->
                        when(value.status) {
                            Resource.Status.SUCCESS -> LoginUiState.SuccessfulLogin(value.data!!)
                            Resource.Status.LOADING -> LoginUiState.ShowLoginForm(LoginFormUiState.LoggingIn)
                            Resource.Status.ERROR -> LoginUiState.ShowLoginForm(LoginFormUiState.LoginFailed(value.resourceError!!))
                        }
                    }
            )
        }
    }

    /**
     * Update the email address.
     */
    fun updateEmailAddress(emailAddress: String) {
        mutableEmailAddress.value = emailAddress
    }

    /**
     * Update the password.
     */
    fun updatePassword(password: String) {
        mutablePassword.value = password
    }
}