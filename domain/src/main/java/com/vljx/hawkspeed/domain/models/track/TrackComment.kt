package com.vljx.hawkspeed.domain.models.track

import com.vljx.hawkspeed.domain.models.user.User

data class TrackComment(
    val commentUid: String,
    val created: Int,
    val text: String,
    val user: User
)