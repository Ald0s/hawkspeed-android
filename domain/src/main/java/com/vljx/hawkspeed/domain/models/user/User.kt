package com.vljx.hawkspeed.domain.models.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val userUid: String,
    val userName: String,
    val bio: String?,
    val privilege: Int,
    val isBot: Boolean,
    val isYou: Boolean
): Parcelable