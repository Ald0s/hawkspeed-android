package com.vljx.hawkspeed.domain.di.component

import com.vljx.hawkspeed.domain.authentication.AuthenticationSession
import com.vljx.hawkspeed.domain.authentication.AuthenticationState
import com.vljx.hawkspeed.domain.di.scope.ApplicationScope
import dagger.hilt.internal.GeneratedComponentManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AuthenticationComponentManager @Inject constructor(
    applicationScope: ApplicationScope,
    private val authenticationSession: AuthenticationSession,
    private val authenticationComponentProvider: Provider<AuthenticationComponentBuilder>
): GeneratedComponentManager<AuthenticationComponent> {
    private val mutableVersionState: MutableStateFlow<ComponentVersion> =
        MutableStateFlow(ComponentVersion.next())
    private var lastAuthenticationState: AuthenticationState =
        authenticationSession.authenticationState.value
    private var authenticationComponent: AuthenticationComponent =
        authenticationComponentProvider
            .get()
            .build()

    val componentVersionState: StateFlow<ComponentVersion> =
        mutableVersionState

    /**
     *
     */
    init {
        applicationScope.launch {
            authenticationSession.authenticationState.collect { value: AuthenticationState ->
                if(lastAuthenticationState == value)
                    return@collect
                Timber.d("Current authentication state has changed. Causing a rebuild now...")
                lastAuthenticationState = value
                rebuildComponent(value)
            }
        }
    }

    private suspend fun rebuildComponent(newAuthenticationState: AuthenticationState) {
        Timber.d("Rebuilding authentication component...")
        authenticationComponent = authenticationComponentProvider
            .get()
            .build()
        mutableVersionState.emit(ComponentVersion.next())
    }

    override fun generatedComponent(): AuthenticationComponent =
        authenticationComponent
}