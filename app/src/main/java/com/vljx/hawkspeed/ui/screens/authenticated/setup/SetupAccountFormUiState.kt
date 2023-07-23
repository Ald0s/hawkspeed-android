package com.vljx.hawkspeed.ui.screens.authenticated.setup

import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleStock
import com.vljx.hawkspeed.ui.component.InputValidationResult

sealed class SetupAccountFormUiState {
    /**
     * A state containing the latest contents of the form.
     */
    data class SetupAccountForm(
        val validateUsername: InputValidationResult,
        val usernameStatusUiState: UsernameStatusUiState,
        val selectedVehicleStock: VehicleStock?,
        val validateBio: InputValidationResult,
        val canAttemptSetupAccount: Boolean
    ): SetupAccountFormUiState()

    /**
     * A state that indicates an attempt to set the account up is in progress.
     */
    object SettingUp: SetupAccountFormUiState()

    /**
     * A failure state that indicates the last attempt to setup the User's account failed.
     */
    data class SetupAccountFailed(
        val resourceError: ResourceError
    ): SetupAccountFormUiState()
}