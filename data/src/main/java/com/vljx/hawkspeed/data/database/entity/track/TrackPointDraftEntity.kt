package com.vljx.hawkspeed.data.database.entity.track

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_point_draft")
data class TrackPointDraftEntity(
    @PrimaryKey
    var trackPointDraftId: Long?,
    var latitude: Double,
    var longitude: Double,
    var loggedAt: Long,
    var speed: Float,
    var rotation: Float,
    var trackDraftId: Long
)