package com.vljx.hawkspeed.ui.screens.authenticated.world

/**
 * A callback interface for resolving location permission & setting issues from composables to main activity.
 */
interface LocationPermissionSettingsCallback {
    /**
     *
     */
    fun locationPermissionsUpdated(
        coarseAccessGranted: Boolean,
        fineAccessGranted: Boolean
    )

    /**
     *
     */
    fun locationSettingsAppropriate(appropriate: Boolean)
}