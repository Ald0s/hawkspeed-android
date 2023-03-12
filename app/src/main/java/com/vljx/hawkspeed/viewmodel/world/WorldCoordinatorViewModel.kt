package com.vljx.hawkspeed.viewmodel.world

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.view.world.CanLoadMapState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class WorldCoordinatorViewModel @Inject constructor(

): ViewModel() {
    private val mutableLocationSettingsAppropriate: MutableSharedFlow<Boolean> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val mutablePreciseLocationPermissionGiven: MutableSharedFlow<Boolean> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val mutableCoarseLocationPermissionGiven: MutableSharedFlow<Boolean> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * A shared flow that will emit a boolean only when both permission states have been updated.
     * The boolean emitted is in reference to all required device permission being given.
     */
    val allDevicePermissionGiven: SharedFlow<Boolean> =
        combine(
            mutablePreciseLocationPermissionGiven,
            mutableCoarseLocationPermissionGiven
        ) { preciseLocation, coarseLocation ->
            preciseLocation && coarseLocation
        }.distinctUntilChanged().shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    /**
     * A shared flow that will emit a state communicating the ability for the app to actually load the Google map. This is a requirement for joining the world,
     * but not a focal point. When this emits load allowed, it will then be determined whether HawkSpeed even wants us participating.
     */
    val canLoadMapState: SharedFlow<CanLoadMapState> =
        combine(
            mutableLocationSettingsAppropriate,
            mutablePreciseLocationPermissionGiven,
            mutableCoarseLocationPermissionGiven
        ) { settings, precise, coarse ->
            return@combine when {
                settings && precise && coarse -> CanLoadMapState.LoadAllowed
                else -> CanLoadMapState.LoadDenied(settings, precise, coarse)
            }
        }.distinctUntilChanged().shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    /**
     * Updates whether the location settings configured on the device are appropriately configured for HawkSpeed.
     */
    fun setLocationSettingsAppropriate(settingsAppropriate: Boolean) {
        mutableLocationSettingsAppropriate.tryEmit(settingsAppropriate)
    }

    /**
     * Update whether the precise and coarse location permissions are both granted.
     */
    fun setLocationPermissions(preciseLocationPermissionGiven: Boolean, coarseLocationPermissionGiven: Boolean) {
        mutablePreciseLocationPermissionGiven.tryEmit(preciseLocationPermissionGiven)
        mutableCoarseLocationPermissionGiven.tryEmit(coarseLocationPermissionGiven)
    }
}