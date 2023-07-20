package com.vljx.hawkspeed.data.models.track

import com.vljx.hawkspeed.data.models.user.UserModel

data class TrackCommentModel(
    val commentUid: String,
    val created: Int,
    val text: String,
    val user: UserModel,
    val trackUid: String
)