package com.vljx.hawkspeed.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    @Expose
    val userUid: String,
    @Expose
    val userName: String,
    @Expose
    val privilege: Int,
    @Expose
    val isBot: Boolean,
    @Expose
    val isYou: Boolean
)