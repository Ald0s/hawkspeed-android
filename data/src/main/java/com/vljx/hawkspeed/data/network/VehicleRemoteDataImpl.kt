package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.vehicle.OurVehiclesModel
import com.vljx.hawkspeed.data.models.vehicle.VehicleModel
import com.vljx.hawkspeed.data.models.vehicle.VehiclesModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleMakesPageModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleModelsPageModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStocksPageModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleTypesPageModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleYearsPageModel
import com.vljx.hawkspeed.data.network.api.VehicleService
import com.vljx.hawkspeed.data.network.mapper.vehicle.OurVehiclesDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.VehicleDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.VehiclesDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.stock.VehicleMakesPageDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.stock.VehicleModelsPageDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.stock.VehicleStocksPageDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.stock.VehicleTypesPageDtoMapper
import com.vljx.hawkspeed.data.network.mapper.vehicle.stock.VehicleYearsPageDtoMapper
import com.vljx.hawkspeed.data.network.models.vehicle.stock.VehicleMakesPageDto
import com.vljx.hawkspeed.data.network.requestmodels.RequestCreateVehicleDto
import com.vljx.hawkspeed.data.source.vehicle.VehicleRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestCreateVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleMakes
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleModels
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleStocks
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleTypes
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleYears
import javax.inject.Inject

class VehicleRemoteDataImpl @Inject constructor(
    private val vehicleService: VehicleService,

    private val vehicleDtoMapper: VehicleDtoMapper,
    private val ourVehiclesDtoMapper: OurVehiclesDtoMapper,
    private val vehiclesDtoMapper: VehiclesDtoMapper,

    private val vehicleMakesPageDtoMapper: VehicleMakesPageDtoMapper,
    private val vehicleTypesPageDtoMapper: VehicleTypesPageDtoMapper,
    private val vehicleModelsPageDtoMapper: VehicleModelsPageDtoMapper,
    private val vehicleYearsPageDtoMapper: VehicleYearsPageDtoMapper,
    private val vehicleStocksPageDtoMapper: VehicleStocksPageDtoMapper
): BaseRemoteData(), VehicleRemoteData {
    override suspend fun createVehicle(requestCreateVehicle: RequestCreateVehicle): Resource<VehicleModel> = getResult({
        vehicleService.createNewVehicle(
            RequestCreateVehicleDto(requestCreateVehicle)
        )
    }, vehicleDtoMapper)

    override suspend fun queryUserVehicleByUid(userUid: String, vehicleUid: String): Resource<VehicleModel> = getResult({
        vehicleService.queryUserVehicle(userUid, vehicleUid)
    }, vehicleDtoMapper)

    override suspend fun queryOurVehicles(): Resource<OurVehiclesModel> = getResult({
        vehicleService.queryOurVehicles()
    }, ourVehiclesDtoMapper)

    override suspend fun queryVehiclesFor(userUid: String): Resource<VehiclesModel> = getResult({
        vehicleService.queryVehiclesFor(userUid)
    }, vehiclesDtoMapper)

    override suspend fun queryPageVehicleMakes(page: Int): Resource<VehicleMakesPageModel> = getResult({
        vehicleService.vehicleMakeSearch(
            page
        )
    }, vehicleMakesPageDtoMapper)

    override suspend fun queryPageVehicleTypes(makeUid: String, page: Int): Resource<VehicleTypesPageModel> = getResult({
        vehicleService.vehicleTypeSearch(
            makeUid,
            page
        )
    }, vehicleTypesPageDtoMapper)

    override suspend fun queryPageVehicleModels(makeUid: String, typeId: String, page: Int): Resource<VehicleModelsPageModel> = getResult({
        vehicleService.vehicleModelSearch(
            makeUid,
            typeId,
            page
        )
    }, vehicleModelsPageDtoMapper)

    override suspend fun queryPageVehicleYears(makeUid: String, typeId: String, modelUid: String, page: Int): Resource<VehicleYearsPageModel> = getResult({
        vehicleService.vehicleYearSearch(
            makeUid,
            typeId,
            modelUid,
            page
        )
    }, vehicleYearsPageDtoMapper)

    override suspend fun queryPageVehicleStocks(makeUid: String, typeId: String, modelUid: String, year: Int, page: Int): Resource<VehicleStocksPageModel> = getResult({
        vehicleService.vehicleStockSearch(
            makeUid,
            typeId,
            modelUid,
            year,
            page
        )
    }, vehicleStocksPageDtoMapper)
}