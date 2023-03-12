package com.vljx.hawkspeed.data.models.account

data class AccountModel(
    val userUid: String,
    val emailAddress: String,
    val userName: String?,
    val isVerified: Boolean,
    val isPasswordVerified: Boolean,
    val isProfileSetup: Boolean
)