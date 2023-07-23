package com.vljx.hawkspeed.data.database.entity.vehicle.stock

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "vehicle_type")
data class VehicleTypeEntity(
    @PrimaryKey
    @Expose
    val typeId: String,
    @Expose
    val typeName: String,
    @Expose
    val description: String
)