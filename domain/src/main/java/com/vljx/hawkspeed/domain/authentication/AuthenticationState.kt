package com.vljx.hawkspeed.domain.authentication

import com.vljx.hawkspeed.domain.models.account.Account

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
        constructor(account: Account):
                this(
                    account.userUid,
                    account.emailAddress,
                    account.userName,
                    account.isVerified,
                    account.isPasswordVerified,
                    account.isProfileSetup,
                    account.canCreateTracks
                )
    }

    object NotAuthenticated: AuthenticationState()
}