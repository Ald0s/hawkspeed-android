package com.vljx.hawkspeed.data.database.entity.race

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.vljx.hawkspeed.data.database.entity.UserEntity

/**
 * A race entity that holds the outcome of a race. Consider this a leaderboard entry, only completed races can be used with this entity. Cancelled or
 * disqualified races are unable to be shown here.
 */
@Entity(tableName = "race_leaderboard")
data class RaceLeaderboardEntity(
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