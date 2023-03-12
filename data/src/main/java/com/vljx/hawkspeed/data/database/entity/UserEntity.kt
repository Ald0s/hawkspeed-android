package com.vljx.hawkspeed.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val userUid: String,
    val userName: String,
    val privilege: Int,
    val isBot: Boolean,
    val isYou: Boolean
)