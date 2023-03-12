package com.vljx.hawkspeed.data.models.user

data class UserModel(
    val userUid: String,
    val userName: String,
    val privilege: Int,
    val isBot: Boolean,
    val isYou: Boolean
)