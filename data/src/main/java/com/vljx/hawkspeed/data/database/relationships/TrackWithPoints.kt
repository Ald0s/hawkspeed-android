package com.vljx.hawkspeed.data.database.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.vljx.hawkspeed.data.database.entity.TrackEntity
import com.vljx.hawkspeed.data.database.entity.TrackPointEntity

data class TrackWithPoints(
    @Embedded
    val track: TrackEntity,

    @Relation(
        parentColumn = "trackUid",
        entityColumn = "trackUid"
    )
    val points: List<TrackPointEntity>
)