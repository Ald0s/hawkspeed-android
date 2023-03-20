package com.vljx.hawkspeed.view.world

sealed class LocationPermissionState {
    object AllGranted: LocationPermissionState()
    object OnlyCoarseGranted: LocationPermissionState()
    object NoneGranted: LocationPermissionState()
}