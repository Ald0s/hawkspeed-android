package com.vljx.hawkspeed.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "vehicle")
data class VehicleEntity(
    @PrimaryKey
    @Expose
    val vehicleUid: String,
    @Expose
    val text: String,
    @Expose
    val belongsToYou: Boolean
)