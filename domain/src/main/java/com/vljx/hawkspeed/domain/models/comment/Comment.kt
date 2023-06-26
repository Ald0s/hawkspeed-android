package com.vljx.hawkspeed.domain.models.comment

import com.vljx.hawkspeed.domain.models.user.User

data class Comment(
    val commentUid: String,
    val created: Int,
    val text: String,
    val user: User
)