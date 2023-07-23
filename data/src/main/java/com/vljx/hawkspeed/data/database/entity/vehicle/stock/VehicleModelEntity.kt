package com.vljx.hawkspeed.data.database.entity.vehicle.stock

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "vehicle_model")
data class VehicleModelEntity(
    @PrimaryKey
    @Expose
    val vehicleModelUid: String,
    @Expose
    val modelName: String,
    @Expose
    val makeUid: String,
    @Embedded(prefix = "type_")
    @Expose
    val type: VehicleTypeEntity
)