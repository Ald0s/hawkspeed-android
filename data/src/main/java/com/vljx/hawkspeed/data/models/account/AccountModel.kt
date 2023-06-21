package com.vljx.hawkspeed.data.models.account

data class AccountModel(
    val userUid: String,
    val emailAddress: String,
    val userName: String?,
    val isAccountVerified: Boolean,
    val isPasswordVerified: Boolean,
    val isProfileSetup: Boolean,
    val canCreateTracks: Boolean
)