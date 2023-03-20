package com.vljx.hawkspeed.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account")
data class AccountEntity(
    @PrimaryKey
    val userUid: String,
    val emailAddress: String,
    val userName: String?,

    val isVerified: Boolean,
    val isPasswordVerified: Boolean,
    val isProfileSetup: Boolean,

    val canCreateTracks: Boolean,

    // A Room only attribute, this will (for now) serve as the unique identifier for the currently logged in
    // account. However...
    // TODO: this must be used alongside some indication of the associated cookie as well, to detect a mismatch.
    // TODO: this is not the right way to do this, revise it please.
    var isInUse: Boolean = false
)