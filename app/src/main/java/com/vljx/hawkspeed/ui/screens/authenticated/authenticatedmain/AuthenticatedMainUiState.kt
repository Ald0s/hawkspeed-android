package com.vljx.hawkspeed.ui.screens.authenticated.authenticatedmain

sealed class AuthenticatedMainUiState {
    // The initial UI state for main, this is ultimately until authentication session can provide state to main view model.
    object Idle: AuthenticatedMainUiState()

    // UI state for when we're authenticated.
    data class Authenticated(
        val userUid: String,
        val emailAddress: String,
        val username: String
    ): AuthenticatedMainUiState()

    // UI state for account not being verified yet.
    data class AuthenticatedAccountNotVerified(
        val userUid: String,
        val emailAddress: String
    ): AuthenticatedMainUiState()

    // UI state for authenticated, the profile is not yet set up.
    data class AuthenticatedSetupRequired(
        val userUid: String,
        val emailAddress: String
    ): AuthenticatedMainUiState()

    // UI state for when we're no longer authenticated.
    object NotAuthenticated: AuthenticatedMainUiState()
}