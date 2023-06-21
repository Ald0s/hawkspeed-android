package com.vljx.hawkspeed.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_point")
data class TrackPointEntity(
    @PrimaryKey
    val trackPointId: Long?,
    val latitude: Double,
    val longitude: Double,
    val trackPathUid: String
)