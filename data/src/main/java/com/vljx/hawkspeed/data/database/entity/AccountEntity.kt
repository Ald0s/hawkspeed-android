package com.vljx.hawkspeed.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account")
data class AccountEntity(
    @PrimaryKey
    val userUid: String,
    val emailAddress: String,
    val userName: String?,

    val isAccountVerified: Boolean,
    val isPasswordVerified: Boolean,
    val isProfileSetup: Boolean,

    val canCreateTracks: Boolean
)