package com.vljx.hawkspeed.data.network.api

import com.vljx.hawkspeed.data.network.models.vehicle.OurVehiclesDto
import retrofit2.Response
import retrofit2.http.GET

interface VehicleService {
    @GET("api/v1/vehicles")
    suspend fun queryOurVehicles(): Response<OurVehiclesDto>
}