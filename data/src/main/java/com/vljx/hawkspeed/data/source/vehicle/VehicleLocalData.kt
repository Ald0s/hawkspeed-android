package com.vljx.hawkspeed.data.source.vehicle

import com.vljx.hawkspeed.data.models.vehicle.OurVehiclesModel
import com.vljx.hawkspeed.data.models.vehicle.VehicleModel
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestDeleteVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestGetVehicle
import kotlinx.coroutines.flow.Flow

interface VehicleLocalData {
    /**
     * Open a flow for a Vehicle from cache.
     */
    fun selectVehicle(requestGetVehicle: RequestGetVehicle): Flow<VehicleModel?>

    /**
     * Open a flow for all Vehicles marked as belonging to the current User.
     */
    fun selectOurVehicles(): Flow<OurVehiclesModel>

    /**
     * Upsert a Vehicle into cache.
     */
    suspend fun upsertVehicle(vehicle: VehicleModel)

    /**
     * Upsert a list of Vehicle into cache.
     */
    suspend fun upsertVehicles(vehicles: List<VehicleModel>)

    /**
     * Delete a Vehicle from cache.
     */
    suspend fun deleteVehicle(requestDeleteVehicle: RequestDeleteVehicle)

    /**
     * Clear all Vehicles from cache.
     */
    suspend fun clearVehicles()
}