package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.vehicle.OurVehicles
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestCreateVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestDeleteVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestGetVehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    /**
     * Open a flow for the desired Vehicle from cache, and also request the latest from server.
     */
    fun getVehicleByUid(requestGetVehicle: RequestGetVehicle): Flow<Resource<Vehicle>>

    /**
     * Open a flow for all Vehicles that belong to the current User, from cache and also request the latest list from server.
     */
    fun getOurVehicles(): Flow<Resource<OurVehicles>>

    /**
     * Perform a request to create a new Vehicle for the current User. This will return a flow for the new Vehicle.
     */
    fun createNewVehicle(requestCreateVehicle: RequestCreateVehicle): Flow<Resource<Vehicle>>

    /**
     * Perform a request to delete the desired Vehicle. This function is asynchronous; the indicated vehicle will be deleted from the database.
     */
    suspend fun deleteVehicleByUid(requestDeleteVehicle: RequestDeleteVehicle)
}