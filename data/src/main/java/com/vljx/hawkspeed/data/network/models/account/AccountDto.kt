package com.vljx.hawkspeed.data.network.models.account

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AccountDto(
    @Expose
    @SerializedName("uid")
    val userUid: String,

    @Expose
    @SerializedName("email_address")
    val emailAddress: String,

    @Expose
    @SerializedName("username")
    val userName: String?,

    @Expose
    @SerializedName("privilege")
    val privilege: Int,

    @Expose
    @SerializedName("account_verified")
    val isVerified: Boolean,

    @Expose
    @SerializedName("password_verified")
    val isPasswordVerified: Boolean,

    @Expose
    @SerializedName("profile_setup")
    val isProfileSetup: Boolean
)