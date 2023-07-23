package com.vljx.hawkspeed.ui.screens.authenticated.choosevehicle

import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleMake
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleModel
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleStock
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleType
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleYear
import kotlinx.coroutines.flow.Flow

sealed class ChooseVehicleUiState {
    /**
     * Provide a paging flow for all vehicle makes.
     */
    data class SelectVehicleMake(
        val vehicleMakesFlow: Flow<PagingData<VehicleMake>>
    ): ChooseVehicleUiState()

    /**
     * Provide a paging flow for all vehicle types within the given vehicle make.
     */
    data class SelectVehicleType(
        val vehicleMake: VehicleMake,
        val vehicleTypesFlow: Flow<PagingData<VehicleType>>
    ): ChooseVehicleUiState()

    /**
     * Provide a paging flow for all vehicle models within the given vehicle make and type.
     */
    data class SelectVehicleModel(
        val vehicleMake: VehicleMake,
        val vehicleType: VehicleType,
        val vehicleModelsFlow: Flow<PagingData<VehicleModel>>
    ): ChooseVehicleUiState()

    /**
     * Provide a paging flow for all vehicle years within the given vehicle make, type and model.
     */
    data class SelectVehicleYear(
        val vehicleMake: VehicleMake,
        val vehicleType: VehicleType,
        val vehicleModel: VehicleModel,
        val vehicleYearsFlow: Flow<PagingData<VehicleYear>>
    ): ChooseVehicleUiState()

    /**
     * Provide a paging flow for all vehicle stocks within the given vehicle make, type, model and years.
     */
    data class SelectVehicleStock(
        val vehicleMake: VehicleMake,
        val vehicleType: VehicleType,
        val vehicleModel: VehicleModel,
        val vehicleYear: VehicleYear,
        val vehicleStocksFlow: Flow<PagingData<VehicleStock>>
    ): ChooseVehicleUiState()

    /**
     * The default loading state.
     */
    object Loading: ChooseVehicleUiState()
}