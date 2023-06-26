package com.vljx.hawkspeed.data.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "race_outcome")
data class RaceOutcomeEntity(
    @PrimaryKey
    @Expose
    val raceUid: String,
    @Expose
    val finishingPlace: Int,
    @Expose
    val started: Long,
    @Expose
    val finished: Long,
    @Expose
    val stopwatch: Int,
    @Embedded(prefix = "race_")
    @Expose
    val player: UserEntity,
    @Expose
    val trackUid: String
)