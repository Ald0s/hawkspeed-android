package com.vljx.hawkspeed.domain.repository

import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.vehicle.OurVehicles
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleMake
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleModel
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleStock
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleType
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleYear
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestCreateVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestDeleteVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestGetVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleMakes
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleModels
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleStocks
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleTypes
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleYears
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    /**
     * Open a flow for the desired User vehicle, by the User's UID and the Vehicle's UID. This will return any currently cached version, but will also request the latest
     * from the remote server.
     */
    fun getUserVehicleByUid(requestGetVehicle: RequestGetVehicle): Flow<Resource<Vehicle>>

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
    suspend fun deleteVehicle(requestDeleteVehicle: RequestDeleteVehicle)

    /**
     * Query a specific vehicle stock from cache.
     */
    fun getVehicleStockFromCache(vehicleStockUid: String): Flow<VehicleStock?>

    /**
     * Open a flow of paging data for vehicle makes.
     */
    fun pageVehicleMakes(requestVehicleMakes: RequestVehicleMakes): Flow<PagingData<VehicleMake>>

    /**
     * Open a flow of paging data for vehicle types.
     */
    fun pageVehicleTypes(requestVehicleTypes: RequestVehicleTypes): Flow<PagingData<VehicleType>>

    /**
     * Open a flow of paging data for vehicle models.
     */
    fun pageVehicleModels(requestVehicleModels: RequestVehicleModels): Flow<PagingData<VehicleModel>>

    /**
     * Open a flow of paging data for vehicle years.
     */
    fun pageVehicleYears(requestVehicleYears: RequestVehicleYears): Flow<PagingData<VehicleYear>>

    /**
     * Open a flow of paging data for vehicle stocks.
     */
    fun pageVehicleStocks(requestVehicleStocks: RequestVehicleStocks): Flow<PagingData<VehicleStock>>
}