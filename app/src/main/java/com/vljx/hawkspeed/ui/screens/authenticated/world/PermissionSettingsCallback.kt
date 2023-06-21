package com.vljx.hawkspeed.ui.screens.authenticated.world

interface PermissionSettingsCallback {
    fun locationPermissionsUpdated(
        coarseAccessGranted: Boolean,
        fineAccessGranted: Boolean
    )

    fun locationSettingsAppropriate(appropriate: Boolean)
}