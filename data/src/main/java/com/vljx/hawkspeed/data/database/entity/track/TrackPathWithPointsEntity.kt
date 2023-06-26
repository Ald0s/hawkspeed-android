package com.vljx.hawkspeed.data.database.entity.track

import androidx.room.Embedded
import androidx.room.Relation

data class TrackPathWithPointsEntity(
    @Embedded
    val trackPath: TrackPathEntity,

    @Relation(
        parentColumn = "trackPathUid",
        entityColumn = "trackPathUid"
    )
    val trackPoints: List<TrackPointEntity>
)