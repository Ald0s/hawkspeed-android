package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.vehicle.OurVehiclesModel
import com.vljx.hawkspeed.data.models.vehicle.VehiclesModel
import com.vljx.hawkspeed.data.network.api.VehicleService
import com.vljx.hawkspeed.data.network.mapper.vehicle.OurVehiclesDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.VehicleDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.VehiclesDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.stock.VehicleMakesPageDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.stock.VehicleModelsPageDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.stock.VehicleTypesPageDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.stock.VehicleYearsPageDtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleMakesPageDto
import com.vljx.hawkspeed.data.source.vehicle.VehicleRemoteData
import com.vljx.hawkspeed.domain.Resource
import javax.inject.Inject

class VehicleRemoteDataImpl @Inject constructor(
    private val vehicleService: VehicleService,

    private val vehicleDtoMapper: VehicleDtoMapper,
    private val ourVehiclesDtoMapper: OurVehiclesDtoMapper,
    private val vehiclesDtoMapper: VehiclesDtoMapper,

    private val vehicleMakesPageDto: VehicleMakesPageDtoMapper,
    private val vehicleTypesPageDtoMapper: VehicleTypesPageDtoMapper,
    private val vehicleModelsPageDtoMapper: VehicleModelsPageDtoMapper,
    private val vehicleYearsPageDtoMapper: VehicleYearsPageDtoMapper,
    private val vehicleStocksPageDtoMapper: VehicleModelsPageDtoMapper
): BaseRemoteData(), VehicleRemoteData {
    override suspend fun queryOurVehicles(): Resource<OurVehiclesModel> = getResult({
        vehicleService.queryOurVehicles()
    }, ourVehiclesDtoMapper)

    override suspend fun queryVehiclesFor(userUid: String): Resource<VehiclesModel> = getResult({
        vehicleService.queryVehiclesFor(userUid)
    }, vehiclesDtoMapper)
}