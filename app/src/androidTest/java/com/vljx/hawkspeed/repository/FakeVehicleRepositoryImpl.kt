package com.vljx.hawkspeed.repository

import com.vljx.hawkspeed.data.BaseRepository
import com.vljx.hawkspeed.data.mapper.vehicle.OurVehiclesMapper
import com.vljx.hawkspeed.data.mapper.vehicle.VehicleMapper
import com.vljx.hawkspeed.data.models.vehicle.VehicleModel
import com.vljx.hawkspeed.data.source.vehicle.VehicleLocalData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.ResourceImpl
import com.vljx.hawkspeed.domain.models.vehicle.OurVehicles
import com.vljx.hawkspeed.domain.models.vehicle.Vehicle
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestCreateVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestDeleteVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestGetVehicle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class FakeVehicleRepositoryImpl @Inject constructor(
    private val vehicleLocalData: VehicleLocalData,

    private val vehicleMapper: VehicleMapper,
    private val ourVehiclesMapper: OurVehiclesMapper
): BaseRepository(), VehicleRepository {
    override fun getVehicleByUid(requestGetVehicle: RequestGetVehicle): Flow<Resource<Vehicle>> {
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
                    requestCreateVehicle.text,
                    true
                )
                // Return as a successful resource.
                ResourceImpl.success(newVehicle)
            },
            cacheResult = { vehicle ->
                vehicleLocalData.upsertVehicle(vehicle)
            }
        )

    override suspend fun deleteVehicleByUid(requestDeleteVehicle: RequestDeleteVehicle) {
        TODO("Not yet implemented")
    }
}