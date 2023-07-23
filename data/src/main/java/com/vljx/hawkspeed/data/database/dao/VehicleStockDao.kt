package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.vehicle.stock.VehicleStockEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class VehicleStockDao: BaseDao<VehicleStockEntity>() {
    @Query("""
        SELECT *
        FROM vehicle_stock
        WHERE vehicleStockUid = :vehicleStockUid
    """)
    abstract fun selectVehicleStockByUid(vehicleStockUid: String): Flow<VehicleStockEntity?>

    @Query("""
        DELETE FROM vehicle_stock
    """)
    abstract suspend fun clearVehicleStocks()
}