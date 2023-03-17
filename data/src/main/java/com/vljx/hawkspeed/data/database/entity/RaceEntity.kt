package com.vljx.hawkspeed.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "race")
data class RaceEntity(
    @PrimaryKey
    val raceUid: String,
    val trackUid: String,
    val started: Long,
    val finished: Long?,
    val isDisqualified: Boolean,
    val disqualificationReason: String?,
    val isCancelled: Boolean
)