package com.vljx.hawkspeed.data.database.entity.vehicle.stock

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_year")
data class VehicleYearEntity(
    @PrimaryKey
    val year: Int
)