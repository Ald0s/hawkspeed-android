package com.vljx.hawkspeed.data.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track")
data class TrackEntity(
    @PrimaryKey
    val trackUid: String,
    val name: String,
    val description: String,
    @Embedded(prefix = "owner_")
    val owner: UserEntity,
    @Embedded(prefix = "start_")
    val startPoint: TrackPointEntity,
    val isVerified: Boolean,
    val numPositiveVotes: Int,
    val numNegativeVotes: Int,
    val yourRating: Boolean?,
    val numComments: Int,
    val canRace: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean
)