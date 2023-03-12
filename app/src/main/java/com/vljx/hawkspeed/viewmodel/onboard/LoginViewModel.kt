package com.vljx.hawkspeed.viewmodel.onboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.interactor.account.CheckLoginUseCase
import com.vljx.hawkspeed.domain.interactor.account.LoginUseCase
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.requests.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val checkLoginUseCase: CheckLoginUseCase
): ViewModel() {
    /**
     * Shared flow to hold the result of the User's login.
     */
    private val mutableLoginResult: MutableSharedFlow<Resource<Account>> = MutableSharedFlow(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )

    val loginResult: Flow<Resource<Account>> =
        mutableLoginResult.distinctUntilChanged()

    val mutableEmailAddress: MutableStateFlow<String?> = MutableStateFlow(null)
    val mutablePassword: MutableStateFlow<String?> = MutableStateFlow(null)

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

    val canLogin: StateFlow<Boolean> =
        combine(
            isEmailAddressValid,
            isPasswordValid
        ) { emailAddressValid, passwordValid ->
            emailAddressValid && passwordValid
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val loginRequest: StateFlow<LoginRequest?> =
        combine(
            mutableEmailAddress,
            mutablePassword
        ) { emailAddress, password ->
            if(emailAddress != null && password != null) {
                return@combine LoginRequest(emailAddress, password, true)
            } else {
                return@combine null
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun login(loginRequest: LoginRequest) {
        viewModelScope.launch {
            // Emit all from login use case to the current account mutable.
            mutableLoginResult.emitAll(
                loginUseCase(loginRequest)
            )
        }
    }

    fun attemptCheckLogin() {
        viewModelScope.launch {
            mutableLoginResult.emitAll(
                checkLoginUseCase(Unit)
            )
        }
    }
}