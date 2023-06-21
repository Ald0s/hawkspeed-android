package com.vljx.hawkspeed.ui

import com.vljx.hawkspeed.ui.screens.authenticated.world.PermissionSettingsCallback

interface MainConfigurePermissions {
    fun resolveLocationPermission(
        permissionSettingsCallback: PermissionSettingsCallback
    )
}