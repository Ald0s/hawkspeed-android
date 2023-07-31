package com.vljx.hawkspeed.ui.screens.authenticated.world

/**
 * * A callback interface for resolving activity recognition permission from composables to main activity.
 */
interface ActivityRecognitionPermissionCallback {
    /**
     * Confirm activity recognition permission has been granted.
     */
    fun activityRecognitionPermissionGranted()

    /**
     * Indicate that activity recognition permission has been refused.
     */
    fun activityRecognitionPermissionRefused()
}