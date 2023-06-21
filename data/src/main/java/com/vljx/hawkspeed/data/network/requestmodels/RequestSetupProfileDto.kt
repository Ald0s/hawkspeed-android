package com.vljx.hawkspeed.data.network.requestmodels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.requestmodels.account.RequestSetupProfile

data class RequestSetupProfileDto(
    //@Expose
    //@SerializedName("profile_image")
    //val profileImage: String?

    @Expose
    @SerializedName("username")
    val userName: String,

    @Expose
    @SerializedName("bio")
    val bio: String?,

    @Expose
    @SerializedName("vehicle_information")
    val vehicleInformation: String
) {
    constructor(requestSetupProfile: RequestSetupProfile):
            this(
                requestSetupProfile.userName,
                requestSetupProfile.bio,
                requestSetupProfile.vehicleInformation
            )
}