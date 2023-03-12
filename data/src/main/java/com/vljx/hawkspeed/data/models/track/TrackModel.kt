package com.vljx.hawkspeed.data.models.track

import com.vljx.hawkspeed.data.models.user.UserModel

data class TrackModel(
    val trackUid: String,
    val name: String,
    val description: String,
    val owner: UserModel,
    val startPoint: TrackPointModel,
    val isVerified: Boolean,
    val canRace: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean
    //val points: List<TrackPointModel>
)