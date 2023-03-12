package com.vljx.hawkspeed.domain.models.account

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Account(
    val userUid: String,
    val emailAddress: String,
    val userName: String?,
    val isVerified: Boolean,
    val isPasswordVerified: Boolean,
    val isProfileSetup: Boolean
): Parcelable {
    val anySetupOrCompletionRequired: Boolean
        get() = !isVerified || !isPasswordVerified || !isProfileSetup

    companion object {
        const val ARG_ACCOUNT = "com.vljx.hawkspeed.domain.models.account.Account.ARG_ACCOUNT"
    }
}