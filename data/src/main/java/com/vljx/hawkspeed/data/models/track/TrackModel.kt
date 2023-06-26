package com.vljx.hawkspeed.data.models.track

import com.vljx.hawkspeed.data.models.race.RaceOutcomeModel
import com.vljx.hawkspeed.data.models.user.UserModel

data class TrackModel(
    val trackUid: String,
    val name: String,
    val description: String,
    val owner: UserModel,
    val topLeaderboard: List<RaceOutcomeModel>,
    val startPoint: TrackPointModel,
    val isVerified: Boolean,
    val numPositiveVotes: Int,
    val numNegativeVotes: Int,
    val yourRating: Boolean?,
    val numComments: Int,
    val canRace: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canComment: Boolean
)