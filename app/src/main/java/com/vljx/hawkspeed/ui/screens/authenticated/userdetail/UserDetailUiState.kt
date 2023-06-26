package com.vljx.hawkspeed.ui.screens.authenticated.userdetail

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.user.User

sealed class UserDetailUiState {
    /**
     * The initial loading state for the User's detail.
     */
    object Loading: UserDetailUiState()

    /**
     * The success state that indicates a new User.
     */
    data class GotUser(
        val user: User
    ): UserDetailUiState()

    /**
     * The failure state for a User's detail.
     */
    data class Failed(
        val resourceError: ResourceError
    ): UserDetailUiState()
}