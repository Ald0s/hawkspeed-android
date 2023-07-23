package com.vljx.hawkspeed.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.vljx.hawkspeed.data.database.entity.vehicle.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class VehicleDao: BaseDao<VehicleEntity>() {
    @Query("""
        SELECT *
        FROM vehicle
        WHERE vehicleUid = :vehicleUid
        LIMIT 1
    """)
    abstract fun selectVehicleByUid(vehicleUid: String): Flow<VehicleEntity?>

    @Query("""
        SELECT *
        FROM vehicle
        WHERE vehicle.user_isYou = 1
    """)
    abstract fun selectOurVehicles(): Flow<List<VehicleEntity>>

    @Query("""
        DELETE FROM vehicle
        WHERE vehicleUid = :vehicleUid
    """)
    abstract suspend fun deleteVehicleByUid(vehicleUid: String)

    @Query("""
        DELETE FROM vehicle
    """)
    abstract suspend fun deleteVehicles()
}