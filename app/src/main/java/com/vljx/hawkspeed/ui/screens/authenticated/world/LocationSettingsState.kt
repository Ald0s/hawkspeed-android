package com.vljx.hawkspeed.ui.screens.authenticated.world

sealed class LocationSettingsState {
    // Location settings are appropriate.
    object Appropriate: LocationSettingsState()

    // Not appropriate.
    object NotAppropriate: LocationSettingsState()
}