package com.vljx.hawkspeed.data.database

import com.vljx.hawkspeed.data.database.dao.VehicleDao
import com.vljx.hawkspeed.data.database.entity.vehicle.OurVehiclesEntity
import com.vljx.hawkspeed.data.database.mapper.vehicle.OurVehiclesEntityMapper
import com.vljx.hawkspeed.data.database.mapper.vehicle.VehicleEntityMapper
import com.vljx.hawkspeed.data.models.vehicle.OurVehiclesModel
import com.vljx.hawkspeed.data.models.vehicle.VehicleModel
import com.vljx.hawkspeed.data.source.vehicle.VehicleLocalData
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestDeleteVehicle
import com.vljx.hawkspeed.domain.requestmodels.vehicle.RequestGetVehicle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VehicleLocalDataImpl @Inject constructor(
    private val vehicleDao: VehicleDao,

    private val vehicleEntityMapper: VehicleEntityMapper,
    private val ourVehiclesEntityMapper: OurVehiclesEntityMapper
): VehicleLocalData {
    override fun selectVehicle(requestGetVehicle: RequestGetVehicle): Flow<VehicleModel?> =
        vehicleDao.selectVehicleByUid(requestGetVehicle.vehicleUid)
            .map { vehicleEntity ->
                vehicleEntity?.let { vehicleEntityMapper.mapFromEntity(it) }
            }

    override fun selectOurVehicles(): Flow<OurVehiclesModel> =
        vehicleDao.selectOurVehicles().map {
            ourVehiclesEntityMapper.mapFromEntity(
                OurVehiclesEntity(it)
            )
        }

    override suspend fun upsertVehicle(vehicle: VehicleModel) =
        vehicleDao.upsert(
            vehicleEntityMapper.mapToEntity(vehicle)
        )

    override suspend fun upsertVehicles(vehicles: List<VehicleModel>) =
        vehicleDao.upsert(
            vehicleEntityMapper.mapToEntityList(vehicles)
        )

    override suspend fun deleteVehicle(requestDeleteVehicle: RequestDeleteVehicle) =
        vehicleDao.deleteVehicleByUid(requestDeleteVehicle.vehicleUid)

    override suspend fun clearVehicles() =
        vehicleDao.deleteVehicles()
}