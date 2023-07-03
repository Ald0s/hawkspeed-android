package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.vehicle.OurVehiclesMapper
import com.vljx.hawkspeed.data.mapper.vehicle.VehicleMapper
import com.vljx.hawkspeed.data.source.vehicle.VehicleLocalData
import com.vljx.hawkspeed.data.source.vehicle.VehicleRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.vehicle.OurVehicles
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestCreateVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestDeleteVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestGetVehicle
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val vehicleLocalData: VehicleLocalData,
    private val vehicleRemoteData: VehicleRemoteData,

    private val vehicleMapper: VehicleMapper,
    private val ourVehiclesMapper: OurVehiclesMapper
): BaseRepository(), VehicleRepository {
    override fun getVehicleByUid(requestGetVehicle: RequestGetVehicle): Flow<Resource<Vehicle>> =
        throw NotImplementedError()

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

    override suspend fun deleteVehicleByUid(requestDeleteVehicle: RequestDeleteVehicle) =
        throw NotImplementedError()
}