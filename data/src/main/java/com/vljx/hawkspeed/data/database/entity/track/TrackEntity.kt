package com.vljx.hawkspeed.data.database.entity.track

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vljx.hawkspeed.data.database.entity.race.RaceLeaderboardEntity
import com.vljx.hawkspeed.data.database.entity.UserEntity
import com.vljx.hawkspeed.domain.enums.TrackType

@Entity(tableName = "track")
data class TrackEntity(
    @PrimaryKey
    val trackUid: String,
    val name: String,
    val description: String,
    @Embedded(prefix = "owner_")
    val owner: UserEntity,
    val topLeaderboard: List<RaceLeaderboardEntity>,
    @Embedded(prefix = "start_")
    val startPoint: TrackPointEntity,
    val isVerified: Boolean,
    val trackType: TrackType,
    val numPositiveVotes: Int,
    val numNegativeVotes: Int,
    val yourRating: Boolean?,
    val numComments: Int,
    val canRace: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canComment: Boolean
)