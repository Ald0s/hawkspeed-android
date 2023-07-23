package com.vljx.hawkspeed.data.source.vehicle

import com.vljx.hawkspeed.data.models.vehicle.OurVehiclesModel
import com.vljx.hawkspeed.data.models.vehicle.VehicleModel
import com.vljx.hawkspeed.data.models.vehicle.VehiclesModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleMakesPageModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleModelsPageModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStocksPageModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleTypesPageModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleYearsPageModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestCreateVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleMakes
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleModels
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleStocks
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleTypes
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleYears

interface VehicleRemoteData {
    /**
     * Perform a request to create a new vehicle for the current User.
     */
    suspend fun createVehicle(requestCreateVehicle: RequestCreateVehicle): Resource<VehicleModel>

    /**
     * Perform a request for a User's vehicle with the UIDs belonging to the User and the desired Vehicle.
     */
    suspend fun queryUserVehicleByUid(userUid: String, vehicleUid: String): Resource<VehicleModel>

    /**
     * Query the current User's list of Vehicles.
     */
    suspend fun queryOurVehicles(): Resource<OurVehiclesModel>

    /**
     * Query the given User's list of vehicles.
     */
    suspend fun queryVehiclesFor(userUid: String): Resource<VehiclesModel>

    /**
     * Perform a query for a page of vehicle makes.
     */
    suspend fun queryPageVehicleMakes(page: Int): Resource<VehicleMakesPageModel>

    /**
     * Perform a query for a page of vehicle types.
     */
    suspend fun queryPageVehicleTypes(makeUid: String, page: Int): Resource<VehicleTypesPageModel>

    /**
     * Perform a query for a page of vehicle models.
     */
    suspend fun queryPageVehicleModels(makeUid: String, typeId: String, page: Int): Resource<VehicleModelsPageModel>

    /**
     * Perform a query for a page of vehicle years.
     */
    suspend fun queryPageVehicleYears(makeUid: String, typeId: String, modelUid: String, page: Int): Resource<VehicleYearsPageModel>

    /**
     * Perform a query for a page of vehicle stocks.
     */
    suspend fun queryPageVehicleStocks(makeUid: String, typeId: String, modelUid: String, year: Int, page: Int): Resource<VehicleStocksPageModel>
}