package com.vljx.hawkspeed.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.vljx.hawkspeed.data.mapper.vehicle.OurVehiclesMapper
import com.vljx.hawkspeed.data.mapper.vehicle.VehicleMapper
import com.vljx.hawkspeed.data.mapper.vehicle.stock.VehicleMakeMapper
import com.vljx.hawkspeed.data.mapper.vehicle.stock.VehicleModelMapper
import com.vljx.hawkspeed.data.mapper.vehicle.stock.VehicleStockMapper
import com.vljx.hawkspeed.data.mapper.vehicle.stock.VehicleTypeMapper
import com.vljx.hawkspeed.data.mapper.vehicle.stock.VehicleYearMapper
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleMakeModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleMakesPageModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleModelModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleModelsPageModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStockModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStocksPageModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleTypeModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleTypesPageModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleYearModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleYearsPageModel
import com.vljx.hawkspeed.data.pagingsource.BasePagingSource
import com.vljx.hawkspeed.data.source.vehicle.VehicleLocalData
import com.vljx.hawkspeed.data.source.vehicle.VehicleRemoteData
import com.vljx.hawkspeed.data.source.vehicle.VehicleStockLocalData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.vehicle.OurVehicles
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleMake
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleModel
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleStock
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleType
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleYear
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestCreateVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestDeleteVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestGetVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleMakes
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleModels
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleStocks
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleTypes
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleYears
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val vehicleLocalData: VehicleLocalData,
    private val vehicleRemoteData: VehicleRemoteData,

    private val vehicleStockLocalData: VehicleStockLocalData,

    private val vehicleMapper: VehicleMapper,
    private val ourVehiclesMapper: OurVehiclesMapper,

    private val vehicleMakeMapper: VehicleMakeMapper,
    private val vehicleTypeMapper: VehicleTypeMapper,
    private val vehicleModelMapper: VehicleModelMapper,
    private val vehicleYearMapper: VehicleYearMapper,
    private val vehicleStockMapper: VehicleStockMapper
): BaseRepository(), VehicleRepository {
    override fun getUserVehicleByUid(requestGetVehicle: RequestGetVehicle): Flow<Resource<Vehicle>> =
        flowQueryFromCacheNetworkAndCache(
            vehicleMapper,
            databaseQuery = { vehicleLocalData.selectVehicle(requestGetVehicle) },
            networkQuery = { vehicleRemoteData.queryUserVehicleByUid(requestGetVehicle.userUid, requestGetVehicle.vehicleUid) },
            cacheResult = { vehicle ->
                vehicleLocalData.upsertVehicle(vehicle)
            }
        )

    override fun getOurVehicles(): Flow<Resource<OurVehicles>> =
        flowQueryFromCacheNetworkAndCache(
            ourVehiclesMapper,
            databaseQuery = { vehicleLocalData.selectOurVehicles() },
            networkQuery = { vehicleRemoteData.queryOurVehicles() },
            cacheResult = { ourVehicles ->
                vehicleLocalData.upsertVehicles(ourVehicles.vehicles)
            }
        )

    override fun createNewVehicle(requestCreateVehicle: RequestCreateVehicle): Flow<Resource<Vehicle>> =
        throw NotImplementedError()

    override suspend fun deleteVehicle(requestDeleteVehicle: RequestDeleteVehicle) =
        throw NotImplementedError()

    override fun getVehicleStockFromCache(vehicleStockUid: String): Flow<VehicleStock?> =
        flowFromCache(
            vehicleStockMapper,
            databaseQuery = { vehicleStockLocalData.selectVehicleStockByUid(vehicleStockUid) }
        )

    override fun pageVehicleMakes(requestVehicleMakes: RequestVehicleMakes): Flow<PagingData<VehicleMake>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE_VEHICLES
            )
        ) {
            object: BasePagingSource<VehicleMakesPageModel, VehicleMakeModel>() {
                override suspend fun performRequest(pageNumber: Int): Resource<VehicleMakesPageModel> =
                    vehicleRemoteData.queryPageVehicleMakes(
                        pageNumber
                    )

                override suspend fun returnPageFrom(pageModel: VehicleMakesPageModel): List<VehicleMakeModel> =
                    pageModel.makes
            }
        }.flow.map { vehicleMakeModelPagingData ->
            vehicleMakeModelPagingData.map { vehicleMakeModel ->
                vehicleMakeMapper.mapFromData(vehicleMakeModel)
            }
        }

    override fun pageVehicleTypes(requestVehicleTypes: RequestVehicleTypes): Flow<PagingData<VehicleType>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE_VEHICLES
            )
        ) {
            object: BasePagingSource<VehicleTypesPageModel, VehicleTypeModel>() {
                override suspend fun performRequest(pageNumber: Int): Resource<VehicleTypesPageModel> =
                    vehicleRemoteData.queryPageVehicleTypes(
                        requestVehicleTypes.makeUid,
                        pageNumber
                    )

                override suspend fun returnPageFrom(pageModel: VehicleTypesPageModel): List<VehicleTypeModel> =
                    pageModel.types
            }
        }.flow.map { vehicleTypeModelPagingData ->
            vehicleTypeModelPagingData.map { vehicleTypeModel ->
                vehicleTypeMapper.mapFromData(vehicleTypeModel)
            }
        }

    override fun pageVehicleModels(requestVehicleModels: RequestVehicleModels): Flow<PagingData<VehicleModel>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE_VEHICLES
            )
        ) {
            object: BasePagingSource<VehicleModelsPageModel, VehicleModelModel>() {
                override suspend fun performRequest(pageNumber: Int): Resource<VehicleModelsPageModel> =
                    vehicleRemoteData.queryPageVehicleModels(
                        requestVehicleModels.makeUid,
                        requestVehicleModels.typeId,
                        pageNumber
                    )

                override suspend fun returnPageFrom(pageModel: VehicleModelsPageModel): List<VehicleModelModel> =
                    pageModel.models
            }
        }.flow.map { vehicleModelModelPagingData ->
            vehicleModelModelPagingData.map { vehicleModelModel ->
                vehicleModelMapper.mapFromData(vehicleModelModel)
            }
        }

    override fun pageVehicleYears(requestVehicleYears: RequestVehicleYears): Flow<PagingData<VehicleYear>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE_VEHICLES
            )
        ) {
            object: BasePagingSource<VehicleYearsPageModel, VehicleYearModel>() {
                override suspend fun performRequest(pageNumber: Int): Resource<VehicleYearsPageModel> =
                    vehicleRemoteData.queryPageVehicleYears(
                        requestVehicleYears.makeUid,
                        requestVehicleYears.typeId,
                        requestVehicleYears.modelUid,
                        pageNumber
                    )

                override suspend fun returnPageFrom(pageModel: VehicleYearsPageModel): List<VehicleYearModel> =
                    pageModel.years
            }
        }.flow.map { vehicleYearModelPagingData ->
            vehicleYearModelPagingData.map { vehicleYearModel ->
                vehicleYearMapper.mapFromData(vehicleYearModel)
            }
        }

    override fun pageVehicleStocks(requestVehicleStocks: RequestVehicleStocks): Flow<PagingData<VehicleStock>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE_VEHICLES
            )
        ) {
            object: BasePagingSource<VehicleStocksPageModel, VehicleStockModel>() {
                override suspend fun performRequest(pageNumber: Int): Resource<VehicleStocksPageModel> =
                    vehicleRemoteData.queryPageVehicleStocks(
                        requestVehicleStocks.makeUid,
                        requestVehicleStocks.typeId,
                        requestVehicleStocks.modelUid,
                        requestVehicleStocks.year,
                        pageNumber
                    )

                override suspend fun returnPageFrom(pageModel: VehicleStocksPageModel): List<VehicleStockModel> =
                    pageModel.vehicles.also {
                        // Upsert all vehicle stocks into cache, so that we are able to query vehicle stocks from cache.
                        vehicleStockLocalData.upsertVehicleStocks(it)
                    }
            }
        }.flow.map { vehicleStockModelPagingData ->
            vehicleStockModelPagingData.map { vehicleStockModel ->
                vehicleStockMapper.mapFromData(vehicleStockModel)
            }
        }

    companion object {
        const val PAGE_SIZE_VEHICLES = 25
    }
}