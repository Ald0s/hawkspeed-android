package com.vljx.hawkspeed.domain.enums

import com.google.gson.annotations.SerializedName

enum class TrackType(trackTypeInt: Int) {
    @SerializedName("0")
    SPRINT(0),
    @SerializedName("1")
    CIRCUIT(1)
}