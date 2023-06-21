package com.vljx.hawkspeed.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TrackWithPathEntity(
    @Embedded
    val track: TrackEntity,

    @Relation(
        entity = TrackPathEntity::class,
        parentColumn = "trackUid",
        entityColumn = "trackPathUid"
    )
    val trackPathWithPoints: TrackPathWithPointsEntity?
)