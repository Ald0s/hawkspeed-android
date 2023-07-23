package com.vljx.hawkspeed.repository

import androidx.paging.PagingData
import com.vljx.hawkspeed.data.BaseRepository
import com.vljx.hawkspeed.data.mapper.vehicle.OurVehiclesMapper
import com.vljx.hawkspeed.data.mapper.vehicle.VehicleMapper
import com.vljx.hawkspeed.data.mapper.vehicle.stock.VehicleStockMapper
import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.data.models.vehicle.VehicleModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleMakeModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleModelModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStockModel
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleTypeModel
import com.vljx.hawkspeed.data.source.vehicle.VehicleLocalData
import com.vljx.hawkspeed.data.source.vehicle.VehicleStockLocalData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.ResourceImpl
import com.vljx.hawkspeed.domain.models.vehicle.OurVehicles
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleMake
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
import java.util.UUID
import javax.inject.Inject

class FakeVehicleRepositoryImpl @Inject constructor(
    private val vehicleLocalData: VehicleLocalData,

    private val vehicleStockLocalData: VehicleStockLocalData,

    private val vehicleStockMapper: VehicleStockMapper,
    private val vehicleMapper: VehicleMapper,
    private val ourVehiclesMapper: OurVehiclesMapper
): BaseRepository(), VehicleRepository {
    override fun getUserVehicleByUid(requestGetVehicle: RequestGetVehicle): Flow<Resource<Vehicle>> {
        TODO("Not yet implemented")
    }

    override fun getOurVehicles(): Flow<Resource<OurVehicles>> =
        flowFromCache(
            ourVehiclesMapper,
            databaseQuery = { vehicleLocalData.selectOurVehicles() }
        ).map {
            it?.let { ResourceImpl.success(it) }
                ?: ResourceImpl.error("no-vehicles", null)
        }

    override fun createNewVehicle(requestCreateVehicle: RequestCreateVehicle): Flow<Resource<Vehicle>> =
        flowQueryNetworkAndCache(
            vehicleMapper,
            networkQuery = {
                // Create a new vehicle.
                val newVehicleUid = UUID.randomUUID().toString()
                val newVehicle = VehicleModel(
                    newVehicleUid,
                    "1994 Toyota Supra",
                    VehicleStockModel(
                        "VEHICLESTOCK01",
                        VehicleMakeModel("MAKE01", "Toyota"),
                        VehicleModelModel("MODEL01", "Supra", "MAKE01", VehicleTypeModel("car", "Car", "Vehicle")),
                        1994,
                        "JZA80",
                        null,
                        "piston",
                        3000,
                        "NA",
                        "P",
                        null,
                        null,
                        "M",
                        5
                    ),
                    UserModel("USER01", "aldos", "Bio", 0, false, true)
                )
                // Return as a successful resource.
                ResourceImpl.success(newVehicle)
            },
            cacheResult = { vehicle ->
                vehicleLocalData.upsertVehicle(vehicle)
            }
        )

    override suspend fun deleteVehicle(requestDeleteVehicle: RequestDeleteVehicle) {
        TODO("Not yet implemented")
    }

    override fun getVehicleStockFromCache(vehicleStockUid: String): Flow<VehicleStock?> =
        flowFromCache(
            vehicleStockMapper,
            databaseQuery = { vehicleStockLocalData.selectVehicleStockByUid(vehicleStockUid) }
        )

    override fun pageVehicleMakes(requestVehicleMakes: RequestVehicleMakes): Flow<PagingData<VehicleMake>> {
        TODO("Not yet implemented")
    }

    override fun pageVehicleTypes(requestVehicleTypes: RequestVehicleTypes): Flow<PagingData<VehicleType>> {
        TODO("Not yet implemented")
    }

    override fun pageVehicleModels(requestVehicleModels: RequestVehicleModels): Flow<PagingData<com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleModel>> {
        TODO("Not yet implemented")
    }

    override fun pageVehicleYears(requestVehicleYears: RequestVehicleYears): Flow<PagingData<VehicleYear>> {
        TODO("Not yet implemented")
    }

    override fun pageVehicleStocks(requestVehicleStocks: RequestVehicleStocks): Flow<PagingData<VehicleStock>> {
        TODO("Not yet implemented")
    }
}