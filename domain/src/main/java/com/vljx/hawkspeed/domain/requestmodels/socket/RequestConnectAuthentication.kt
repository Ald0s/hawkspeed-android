package com.vljx.hawkspeed.domain.requestmodels.socket

data class RequestConnectAuthentication(
    val latitude: Double,
    val longitude: Double,
    val rotation: Float,
    val speed: Float,
    val loggedAt: Long
)