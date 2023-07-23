package com.vljx.hawkspeed.ui.screens.authenticated.vehicledetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(

    private val savedStateHandle: SavedStateHandle,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
): ViewModel() {
    /**
     * A UID for the User who owns the desired vehicle.
     */
    private val mutableSelectedUserUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_USER_UID]))

    /**
     * A UID for the desired vehicle to view detail for.
     */
    private val mutableSelectedVehicleUid: MutableStateFlow<String> = MutableStateFlow(checkNotNull(savedStateHandle[ARG_VEHICLE_UID]))

    companion object {
        const val ARG_USER_UID = "userUid"
        const val ARG_VEHICLE_UID = "vehicleUid"
    }
}