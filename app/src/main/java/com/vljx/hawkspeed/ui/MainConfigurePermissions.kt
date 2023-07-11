package com.vljx.hawkspeed.ui

import com.vljx.hawkspeed.ui.screens.authenticated.world.PermissionSettingsCallback

interface MainConfigurePermissions {
    /**
     * Trigger a location permission resolution workflow.
     */
    fun resolveLocationPermission(
        permissionSettingsCallback: PermissionSettingsCallback
    )
}