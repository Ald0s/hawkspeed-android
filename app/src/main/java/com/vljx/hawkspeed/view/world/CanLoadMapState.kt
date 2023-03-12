package com.vljx.hawkspeed.view.world

sealed class CanLoadMapState {
    object LoadAllowed: CanLoadMapState()
    data class LoadDenied(
        val deviceSettingsAppropriate: Boolean,
        val preciseLocationPermissionGiven: Boolean,
        val coarseLocationPermissionGiven: Boolean
    ): CanLoadMapState()
}