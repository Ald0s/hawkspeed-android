package com.vljx.hawkspeed.data.database.entity.track

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vljx.hawkspeed.data.database.entity.UserEntity

@Entity(tableName = "track_comment")
data class TrackCommentEntity(
    @PrimaryKey
    val commentUid: String,
    val created: Int,
    val text: String,
    @Embedded(prefix = "creator_")
    val user: UserEntity,
    val trackUid: String
)