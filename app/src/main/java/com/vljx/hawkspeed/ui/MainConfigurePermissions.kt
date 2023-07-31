package com.vljx.hawkspeed.ui

import com.vljx.hawkspeed.ui.screens.authenticated.world.ActivityRecognitionPermissionCallback
import com.vljx.hawkspeed.ui.screens.authenticated.world.LocationPermissionSettingsCallback

interface MainConfigurePermissions {
    /**
     * Trigger a location permission resolution workflow.
     */
    fun resolveLocationPermission(
        locationPermissionSettingsCallback: LocationPermissionSettingsCallback
    )

    /**
     * Trigger an activity recognition permission resolution workflow.
     */
    fun resolveActivityRecognitionPermission(
        activityRecognitionPermissionCallback: ActivityRecognitionPermissionCallback
    )
}