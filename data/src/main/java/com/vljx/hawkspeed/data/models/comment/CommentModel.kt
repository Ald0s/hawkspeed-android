package com.vljx.hawkspeed.data.models.comment

import com.vljx.hawkspeed.data.models.user.UserModel

data class CommentModel(
    val commentUid: String,
    val created: Int,
    val text: String,
    val user: UserModel
)