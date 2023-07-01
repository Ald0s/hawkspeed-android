package com.vljx.hawkspeed.data.database.entity.track

import androidx.room.Embedded
import androidx.room.Relation

data class TrackDraftWithPointsEntity(
    @Embedded
    val trackDraft: TrackDraftEntity,

    @Relation(
        parentColumn = "trackDraftId",
        entityColumn = "trackDraftId"
    )
    val draftPoints: List<TrackPointDraftEntity>
)