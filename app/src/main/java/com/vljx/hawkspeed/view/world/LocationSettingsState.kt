package com.vljx.hawkspeed.view.world

sealed class LocationSettingsState {
    object Appropriate: LocationSettingsState()
    object NotAppropriate: LocationSettingsState()
}