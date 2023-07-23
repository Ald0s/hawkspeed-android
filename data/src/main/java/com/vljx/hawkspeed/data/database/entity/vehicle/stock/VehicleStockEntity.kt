package com.vljx.hawkspeed.data.database.entity.vehicle.stock

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "vehicle_stock")
data class VehicleStockEntity(
    @PrimaryKey
    @Expose
    val vehicleStockUid: String,
    @Embedded(prefix = "mk_")
    @Expose
    val make: VehicleMakeEntity,
    @Embedded(prefix = "mdl_")
    @Expose
    val model: VehicleModelEntity,
    @Expose
    val year: Int,
    @Expose
    val version: String?,
    @Expose
    val badge: String?,
    @Expose
    val motorType: String,
    @Expose
    val displacement: Int,
    @Expose
    val induction: String?,
    @Expose
    val fuelType: String?,
    @Expose
    val power: Int?,
    @Expose
    val electricType: String?,
    @Expose
    val transmissionType: String?,
    @Expose
    val numGears: Int?
)