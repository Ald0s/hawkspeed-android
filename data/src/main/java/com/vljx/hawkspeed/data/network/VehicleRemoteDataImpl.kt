package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.vehicle.OurVehiclesModel
import com.vljx.hawkspeed.data.network.api.VehicleService
import com.vljx.hawkspeed.data.network.mapper.vehicle.OurVehiclesDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.VehicleDtoMapper
import com.vljx.hawkspeed.data.source.vehicle.VehicleRemoteData
import com.vljx.hawkspeed.domain.Resource
import javax.inject.Inject

class VehicleRemoteDataImpl @Inject constructor(
    private val vehicleService: VehicleService,

    private val vehicleDtoMapper: VehicleDtoMapper,
    private val ourVehiclesDtoMapper: OurVehiclesDtoMapper
): BaseRemoteData(), VehicleRemoteData {
    override suspend fun queryOurVehicles(): Resource<OurVehiclesModel> = getResult({
        vehicleService.queryOurVehicles()
    }, ourVehiclesDtoMapper)
}