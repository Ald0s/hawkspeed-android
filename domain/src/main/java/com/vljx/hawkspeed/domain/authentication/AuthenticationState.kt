package com.vljx.hawkspeed.domain.authentication

sealed class AuthenticationState {
    data class Authenticated(
        val userUid: String,
        val emailAddress: String,
        val userName: String?,
        val isVerified: Boolean,
        val isPasswordVerified: Boolean,
        val isProfileSetup: Boolean,
        val canCreateTracks: Boolean
    ): AuthenticationState() {
        val anySetupOrCompletionRequired: Boolean
            get() = !isVerified || !isPasswordVerified || !isProfileSetup
    }

    object NotAuthenticated: AuthenticationState()
}