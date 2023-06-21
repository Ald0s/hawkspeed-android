package com.vljx.hawkspeed.domain.models.account

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Account(
    val userUid: String,
    val emailAddress: String,
    val userName: String?,
    val isAccountVerified: Boolean,
    val isPasswordVerified: Boolean,
    val isProfileSetup: Boolean,
    val canCreateTracks: Boolean
): Parcelable