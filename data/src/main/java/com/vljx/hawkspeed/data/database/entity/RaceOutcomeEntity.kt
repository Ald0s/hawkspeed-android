package com.vljx.hawkspeed.data.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "race_outcome")
data class RaceOutcomeEntity(
    @PrimaryKey
    val raceUid: String,
    val started: Long,
    val finished: Long,
    val stopwatch: Int,
    @Embedded(prefix = "race_")
    val player: UserEntity,
    val trackUid: String
)