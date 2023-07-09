package com.vljx.hawkspeed.data.database.entity.track

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vljx.hawkspeed.domain.enums.TrackType

@Entity(tableName = "track_draft")
data class TrackDraftEntity(
    @PrimaryKey
    var trackDraftId: Long?,
    val trackType: TrackType,
    var name: String?,
    var description: String?
)