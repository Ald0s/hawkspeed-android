package com.vljx.hawkspeed.data.database.entity.track

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_path")
data class TrackPathEntity(
    @PrimaryKey
    val trackPathUid: String,
    val hash: String
)