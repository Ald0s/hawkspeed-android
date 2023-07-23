package com.vljx.hawkspeed.data.database.entity.vehicle

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.vljx.hawkspeed.data.database.entity.UserEntity
import com.vljx.hawkspeed.data.database.entity.vehicle.stock.VehicleStockEntity

@Entity(tableName = "vehicle")
data class VehicleEntity(
    @PrimaryKey
    @Expose
    val vehicleUid: String,
    @Expose
    val title: String,
    @Expose
    @Embedded(prefix = "stock_")
    val vehicleStock: VehicleStockEntity,
    @Expose
    @Embedded(prefix = "user_")
    val user: UserEntity
)