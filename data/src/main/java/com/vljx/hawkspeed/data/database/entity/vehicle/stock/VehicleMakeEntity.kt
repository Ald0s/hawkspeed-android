package com.vljx.hawkspeed.data.database.entity.vehicle.stock

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "vehicle_make")
data class VehicleMakeEntity(
    @PrimaryKey
    @Expose
    val vehicleMakeUid: String,
    @Expose
    val makeName: String,
    @Expose
    val logoUrl: String?
)