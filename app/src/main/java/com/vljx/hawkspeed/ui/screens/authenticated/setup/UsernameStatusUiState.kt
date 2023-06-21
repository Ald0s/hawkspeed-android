package com.vljx.hawkspeed.ui.screens.authenticated.setup

sealed class UsernameStatusUiState {
    // Initial state, to also be used when username can't be checked yet.
    object Idle: UsernameStatusUiState()

    // State for query being performed.
    object QueryingStatus: UsernameStatusUiState()

    // Result state for the username being taken.
    data class UsernameTaken(
        val username: String
    ): UsernameStatusUiState()

    // Result state for the username not being taken.
    data class UsernameAvailable(
        val username: String
    ): UsernameStatusUiState()
}