package com.vljx.hawkspeed.data.database.entity.race

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An entity for storing the state of a specific race attempt/instance.
 * There are no conditions on what type of races can be stored in this entity.
 */
@Entity(tableName = "race")
data class RaceEntity(
    @PrimaryKey
    val raceUid: String,
    val trackUid: String,
    val started: Long,
    val finished: Long?,
    val isDisqualified: Boolean,
    val disqualificationReason: String?,
    val isCancelled: Boolean,
    val averageSpeed: Int?,
    val numLapsComplete: Int?,
    val percentComplete: Int?
)