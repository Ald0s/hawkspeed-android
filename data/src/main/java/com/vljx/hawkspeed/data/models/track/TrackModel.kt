package com.vljx.hawkspeed.data.models.track

import com.vljx.hawkspeed.data.models.race.RaceLeaderboardModel
import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.domain.enums.TrackType

data class TrackModel(
    val trackUid: String,
    val name: String,
    val description: String,
    val owner: UserModel,
    val topLeaderboard: List<RaceLeaderboardModel>,
    val startPoint: TrackPointModel,
    val startPointBearing: Float,
    val isVerified: Boolean,
    val length: Int,
    val isSnappedToRoads: Boolean,
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