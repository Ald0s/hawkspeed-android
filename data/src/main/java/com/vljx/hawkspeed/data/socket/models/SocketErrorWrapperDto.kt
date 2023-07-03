package com.vljx.hawkspeed.data.socket.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.domain.base.SocketErrorWrapper

data class SocketErrorWrapperDto(
    @Expose
    @SerializedName("name")
    override val name: String,

    @Expose
    @SerializedName("reason")
    override val reason: String,

    @Expose
    @SerializedName("error_dict")
    override val errorInformation: HashMap<String, String>
): SocketErrorWrapper