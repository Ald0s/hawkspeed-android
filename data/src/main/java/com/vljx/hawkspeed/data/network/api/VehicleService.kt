package com.vljx.hawkspeed.data.network.api

import com.vljx.hawkspeed.data.network.models.vehicle.OurVehiclesDto
import com.vljx.hawkspeed.data.network.models.vehicle.VehiclesDto
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleMakesPageDto
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleModelsPageDto
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleStocksPageDto
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleTypesPageDto
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleYearsPageDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VehicleService {
    @GET("api/v1/vehicles")
    suspend fun queryOurVehicles(): Response<OurVehiclesDto>

    @GET("api/v1/user/{userUid}/vehicles")
    suspend fun queryVehiclesFor(
        @Path(value = "userUid") userUid: String
    ): Response<VehiclesDto>

    @GET("api/v1/vehicles/stock")
    suspend fun vehicleMakeSearch(
        @Query("p") page: Int = 1
    ): Response<VehicleMakesPageDto>

    @GET("api/v1/vehicles/stock")
    suspend fun vehicleTypeSearch(
        @Query("mk") makeUid: String,
        @Query("p") page: Int = 1
    ): Response<VehicleTypesPageDto>

    @GET("api/v1/vehicles/stock")
    suspend fun vehicleModelSearch(
        @Query("mk") makeUid: String,
        @Query("t") typeId: String,
        @Query("p") page: Int = 1
    ): Response<VehicleModelsPageDto>

    @GET("api/v1/vehicles/stock")
    suspend fun vehicleYearSearch(
        @Query("mk") makeUid: String,
        @Query("t") typeId: String,
        @Query("mdl") modelUid: String,
        @Query("p") page: Int = 1
    ): Response<VehicleYearsPageDto>

    @GET("api/v1/vehicles/stock")
    suspend fun vehicleStockSearch(
        @Query("mk") makeUid: String,
        @Query("t") typeId: String,
        @Query("mdl") modelUid: String,
        @Query("y") year: Int,
        @Query("p") page: Int = 1
    ): Response<VehicleStocksPageDto>
}