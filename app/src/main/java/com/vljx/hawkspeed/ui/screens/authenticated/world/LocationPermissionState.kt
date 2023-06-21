package com.vljx.hawkspeed.ui.screens.authenticated.world

sealed class LocationPermissionState {
    // Both fine and coarse location access granted.
    object AllGranted: LocationPermissionState()

    // Only coarse location access granted. This will exclude the Player from creating tracks.
    object OnlyCoarseGranted: LocationPermissionState()

    // No location access granted. The Player will not be able to participate.
    object NoneGranted: LocationPermissionState()
}