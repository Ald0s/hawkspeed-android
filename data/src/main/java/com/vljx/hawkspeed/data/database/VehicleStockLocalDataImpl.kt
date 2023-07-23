package com.vljx.hawkspeed.data.database

import com.vljx.hawkspeed.data.database.dao.VehicleStockDao
import com.vljx.hawkspeed.data.database.mapper.vehicle.stock.VehicleStockEntityMapper
import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStockModel
import com.vljx.hawkspeed.data.source.vehicle.VehicleStockLocalData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VehicleStockLocalDataImpl @Inject constructor(
    private val vehicleStockDao: VehicleStockDao,

    private val vehicleStockEntityMapper: VehicleStockEntityMapper
): VehicleStockLocalData {
    override fun selectVehicleStockByUid(vehicleStockUid: String): Flow<VehicleStockModel?> =
        vehicleStockDao.selectVehicleStockByUid(vehicleStockUid)
            .map { vehicleStockEntity ->
                vehicleStockEntity?.let {
                    vehicleStockEntityMapper.mapFromEntity(it)
                }
            }

    override suspend fun upsertVehicleStocks(vehicleStocks: List<VehicleStockModel>) =
        vehicleStockDao.upsert(
            vehicleStockEntityMapper.mapToEntityList(vehicleStocks)
        )

    override suspend fun upsertVehicleStock(vehicleStock: VehicleStockModel) =
        vehicleStockDao.upsert(
            vehicleStockEntityMapper.mapToEntity(vehicleStock)
        )

    override suspend fun clearVehicleStocks() =
        vehicleStockDao.clearVehicleStocks()
}