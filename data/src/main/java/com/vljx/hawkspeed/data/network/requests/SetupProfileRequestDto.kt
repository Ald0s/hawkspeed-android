package com.vljx.hawkspeed.data.network.requests

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.requests.SetupProfileRequest

data class SetupProfileRequestDto(
    @Expose
    @SerializedName("username")
    val userName: String,

    @Expose
    @SerializedName("bio")
    val bio: String?,

    //@Expose
    //@SerializedName("profile_image")
    //val profileImage: String?
) {
    constructor(setupProfileRequest: SetupProfileRequest):
            this(
                setupProfileRequest.userName,
                setupProfileRequest.bio
            )
}