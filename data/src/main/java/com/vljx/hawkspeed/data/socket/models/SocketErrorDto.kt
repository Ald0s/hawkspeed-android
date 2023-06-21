package com.vljx.hawkspeed.data.socket.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SocketErrorDto(
    @Expose
    @SerializedName("severity")
    val severity: String,

    @Expose
    @SerializedName("name")
    val name: String,

    @Expose
    @SerializedName("error")
    val errorInformation: HashMap<String, String>
)