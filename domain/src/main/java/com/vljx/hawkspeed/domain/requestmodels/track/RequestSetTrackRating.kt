package com.vljx.hawkspeed.domain.requestmodels.track

data class RequestSetTrackRating(
    val trackUid: String,
    val rating: Boolean
)